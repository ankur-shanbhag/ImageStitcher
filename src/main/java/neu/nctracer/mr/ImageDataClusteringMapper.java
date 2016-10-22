package neu.nctracer.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import neu.nctracer.data.DataObject;
import neu.nctracer.data.ImageData;
import neu.nctracer.dm.cluster.Clusterer;
import neu.nctracer.dm.cluster.DBSCANCluster;
import neu.nctracer.exception.DataParsingException;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.utils.DataParser;
import neu.nctracer.utils.DataTransformer;
import neu.nctracer.utils.HdfsFileUtils;

public class ImageDataClusteringMapper extends Mapper<LongWritable, Text, Text, NullWritable> {

    private Collection<DataObject> sourceImageData = null;
    private Collection<DataObject> targetImageData = null;

    private double threshold = 0.0;

    private final Text TEXT_KEY = new Text();

    @Override
    protected void setup(Mapper<LongWritable, Text, Text, NullWritable>.Context context)
            throws IOException,
            InterruptedException {
        super.setup(context);

        try {
            Configuration conf = context.getConfiguration();

            threshold = conf.getDouble(HdfsConstants.IMAGE_MATCHING_ERROR, 0.0);

            String sourceFileData = HdfsFileUtils.readFileAsString(conf,
                                                                   conf.get(HdfsConstants.SOURCE_IMAGE_HDFS_PATH));
            sourceImageData = DataParser.parseImageData(sourceFileData, ImageData.class);

            String targetFileData = HdfsFileUtils.readFileAsString(conf,
                                                                   conf.get(HdfsConstants.TARGET_IMAGE_HDFS_PATH));
            targetImageData = DataParser.parseImageData(targetFileData, ImageData.class);

        } catch (HdfsException e) {
            throw new IOException("Error while reading image data.", e);
        } catch (DataParsingException e) {
            throw new IOException("Error while parsing image data.", e);
        }
    }

    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException,
            InterruptedException {

        String delimiter = context.getConfiguration().get("image.matching.cluster.params.delimiter",
                                                          "\\s*,\\s*");
        String[] config = value.toString().split(delimiter);
        int minPoints = Integer.parseInt(config[0]);
        double eps = Double.parseDouble(config[1]);

        Clusterer clusterer = new DBSCANCluster(minPoints, eps);
        List<List<DataObject>> sourceClusters = clusterer.createClusters(sourceImageData);
        List<List<DataObject>> targetClusters = clusterer.createClusters(targetImageData);

        Map<Integer, List<List<DataObject>>> lookup = new HashMap<>();

        for (List<DataObject> cluster : sourceClusters) {
            if (lookup.containsKey(cluster.size())) {
                lookup.get(cluster.size()).add(cluster);
            } else {
                List<List<DataObject>> list = new ArrayList<>();
                list.add(cluster);
                lookup.put(cluster.size(), list);
            }
        }

        for (List<DataObject> targetCluster : targetClusters) {
            if (lookup.containsKey(targetCluster.size())) {
                List<List<DataObject>> clusters = lookup.get(targetCluster.size());

                for (List<DataObject> sourceCluster : clusters) {
                    // if (checkIfSimilar(sourceCluster, targetCluster)) {
                    TEXT_KEY.set(toString(sourceCluster) + "," + toString(targetCluster));
                    context.write(TEXT_KEY, NullWritable.get());
                    // }
                }
            }
        }
    }

    private void checkIfSimilar(Collection<DataObject> sourceCluster,
                                Collection<DataObject> targetCluster) {

        DataObject sourceCenterPoint = new ImageData();
        sourceCenterPoint.setFeatures(DataTransformer.computeArithmeticMean(sourceCluster));

        DataObject targetCenterPoint = new ImageData();
        targetCenterPoint.setFeatures(DataTransformer.computeArithmeticMean(targetCluster));

        Map<DataObject, double[]> relativeMovementMap = relativePositions(sourceCluster,
                                                                          sourceCenterPoint);
        Map<DataObject, double[]> translatedPoints = DataTransformer
                .doTranslation(targetCenterPoint, relativeMovementMap);
    }

    private Map<DataObject, double[]> relativePositions(Collection<DataObject> cluster,
                                                        DataObject centerPoint) {
        Map<DataObject, double[]> relativeMovementMap = new HashMap<>();
        for (DataObject point : cluster) {
            double[] movement = DataTransformer.computeRelativePosition(centerPoint, point);
            relativeMovementMap.put(point, movement);
        }
        return relativeMovementMap;
    }

    private String toString(Collection<DataObject> points) {
        StringBuilder builder = new StringBuilder("[");
        for (DataObject point : points) {
            builder.append(point).append(",");
        }
        builder.delete(builder.length() - 1, builder.length());
        return builder.append("]").toString();
    }
}