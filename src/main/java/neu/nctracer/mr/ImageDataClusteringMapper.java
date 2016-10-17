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
import neu.nctracer.dm.cluster.Clusterer;
import neu.nctracer.dm.cluster.DBSCANCluster;
import neu.nctracer.exception.DataParsingException;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.utils.DataAnalyser;
import neu.nctracer.utils.DataParser;
import neu.nctracer.utils.HdfsFileUtils;

public class ImageDataClusteringMapper extends Mapper<LongWritable, Text, Text, NullWritable> {

    private Collection<DataObject> sourceImageData = null;
    private Collection<DataObject> targetImageData = null;

    private double threshold = 0.0;

    private final Text TEXT_KEY = new Text();

    @Override
    protected void setup(Mapper<LongWritable, Text, Text, NullWritable>.Context context)
            throws IOException, InterruptedException {
        super.setup(context);

        try {
            Configuration conf = context.getConfiguration();

            threshold = conf.getDouble(HdfsConstants.IMAGE_MATCHING_ERROR, 0.0);

            String sourceFileData = HdfsFileUtils.readFileAsString(conf,
                                                                   conf.get(HdfsConstants.SOURCE_IMAGE_HDFS_PATH));
            sourceImageData = DataParser.parseImageData(sourceFileData);

            String targetFileData = HdfsFileUtils.readFileAsString(conf,
                                                                   conf.get(HdfsConstants.TARGET_IMAGE_HDFS_PATH));
            targetImageData = DataParser.parseImageData(targetFileData);

        } catch (HdfsException e) {
            throw new IOException("Error while reading image data.", e);
        } catch (DataParsingException e) {
            throw new IOException("Error while parsing image data.", e);
        }
    }

    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String delimiter = context.getConfiguration().get("image.matching.cluster.params.delimiter",
                                                          "\\s*,\\s*");
        String[] config = value.toString().split(delimiter);
        int minPoints = Integer.parseInt(config[0]);
        double eps = Double.parseDouble(config[1]);

        Clusterer clusterer = new DBSCANCluster(minPoints, eps);
        List<List<DataObject>> sourceClusters = clusterer.createClusters(sourceImageData);
        List<List<DataObject>> targetClusters = clusterer.createClusters(targetImageData);

        int imageWidth = Integer.parseInt(context.getConfiguration().get("image.width", "1000"));
        int imageHeight = Integer.parseInt(context.getConfiguration().get("image.height", "1500"));

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
//                    if (checkIfSimilar(sourceCluster, targetCluster)) {
                        TEXT_KEY.set(toString(sourceCluster) + "," + toString(targetCluster));
                        context.write(TEXT_KEY, NullWritable.get());
//                    }
                }
            }
        }
    }

    private void checkIfSimilar(Collection<DataObject> sourceCluster,
                                   Collection<DataObject> targetCluster) {

        double[] sourceCenterPoint = DataAnalyser.computeArithmeticMean(sourceCluster);
        double[] targetCenterPoint = DataAnalyser.computeArithmeticMean(targetCluster);

        
//        double[] distanceFromSourceCenter = DataAnalyser
//                .computeEuclideanDistance(new ImageData(sourceCenterPoint),
//                                          sourceCluster);
//        double[] distanceFromTargetCenter = DataAnalyser
//                .computeEuclideanDistance(new ImageData(targetCenterPoint),
//                                          targetCluster);
//
//        Arrays.sort(distanceFromSourceCenter);
//        Arrays.sort(distanceFromTargetCenter);
//
//        for (int i = 0; i < distanceFromSourceCenter.length; i++) {
//            if (Math.abs(distanceFromSourceCenter[i] - distanceFromTargetCenter[i]) > threshold)
//                return false;
//        }
//
//        System.out.println("Match found ...");
    }

    // private boolean checkIfSimilar(List<DataObject> sourceCluster,
    // List<DataObject> targetCluster) {
    //
    // Map<DataObject, List<DataObjectRelation>> sourceRelationMap =
    // computeRelations(sourceCluster);
    // Map<DataObject, List<DataObjectRelation>> targetRelationMap =
    // computeRelations(targetCluster);
    //
    // return false;
    // }
    //
    // private void matchDataObjects(List<DataObjectRelation> sourceObjects,
    // List<DataObjectRelation> targetObjects) {
    //
    // for (int i = 0; i < sourceObjects.size(); i++) {
    // for (int j = 0; j < targetObjects.size(); j++) {
    // double err = computeErr(sourceObjects.get(i), targetObjects.get(j));
    // }
    // }
    // }
    //
    // private double computeErr(DataObjectRelation obj1, DataObjectRelation
    // obj2) {
    // double error = (obj1.getDistance() - obj2.getDistance()) * 0.5;
    //
    // for (int i = 0; i < obj1.getAngles().length; i++) {
    // error += obj1.getAngles()[i] - obj2.getAngles()[i];
    // }
    //
    // return error;
    // }
    //
    // private Map<DataObject, List<DataObjectRelation>>
    // computeRelations(List<DataObject> cluster) {
    //
    // Map<DataObject, List<DataObjectRelation>> allNeighborsRelationMap = new
    // HashMap<>();
    //
    // for (int i = 0; i < cluster.size(); i++) {
    // DataObject dataObject = cluster.get(i);
    // List<DataObjectRelation> relations = new ArrayList<>();
    // allNeighborsRelationMap.put(dataObject, relations);
    //
    // for (int j = 0; j < cluster.size(); j++) {
    // if (i == j)
    // continue;
    // DataObject neighbor = cluster.get(j);
    // DataObjectRelation relation = new ImageDataRelation();
    // relation.setDataObjects(dataObject, neighbor);
    // relation.computeRelation();
    // relations.add(relation);
    // }
    // }
    //
    // return allNeighborsRelationMap;
    // }

    private String toString(Collection<DataObject> points) {
        StringBuilder builder = new StringBuilder("[");
        for (DataObject point : points) {
            builder.append(point).append(",");
        }
        builder.delete(builder.length() - 1, builder.length());
        return builder.append("]").toString();
    }
}
