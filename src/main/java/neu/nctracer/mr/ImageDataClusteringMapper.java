package neu.nctracer.mr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import neu.nctracer.data.DataCorrespondence;
import neu.nctracer.data.DataObject;
import neu.nctracer.data.ImageData;
import neu.nctracer.data.RigidTranformation;
import neu.nctracer.dm.ConfigurationParams;
import neu.nctracer.dm.DMConfigurationHandler;
import neu.nctracer.dm.cluster.Clusterer;
import neu.nctracer.dm.cluster.DBSCANCluster;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.exception.ParsingException;
import neu.nctracer.utils.DataParser;
import neu.nctracer.utils.DataTransformer;
import neu.nctracer.utils.HdfsFileUtils;

public class ImageDataClusteringMapper extends Mapper<LongWritable, Text, Text, NullWritable> {

    private Collection<DataObject> sourceImageData = null;
    private Collection<DataObject> targetImageData = null;

    private double threshold = 0.0;

    private final Text TEXT_KEY = new Text();

    private DMConfigurationHandler configParamHandler = DMConfigurationHandler.getDMConfigurationHandler();

    @Override
    protected void
              setup(Mapper<LongWritable, Text, Text, NullWritable>.Context context) throws IOException,
                                                                                    InterruptedException {
        super.setup(context);

        Configuration conf = context.getConfiguration();

        threshold = conf.getDouble(HdfsConstants.IMAGE_MATCHING_ERROR, 0.0);

        String sourceFileData = HdfsFileUtils.readFileAsString(conf,
                                                               conf.get(HdfsConstants.SOURCE_IMAGE_HDFS_PATH));
        String targetFileData = HdfsFileUtils.readFileAsString(conf,
                                                               conf.get(HdfsConstants.TARGET_IMAGE_HDFS_PATH));
        try {
            sourceImageData = DataParser.parseImageData(sourceFileData, ImageData.class);
            targetImageData = DataParser.parseImageData(targetFileData, ImageData.class);
        } catch (ParsingException e) {
            throw new IOException("Error while parsing image data.", e);
        }
    }

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException,
                                                                   InterruptedException {
        String delimiter = context.getConfiguration().get("image.matching.cluster.params.delimiter",
                                                          "\\s*,\\s*");

        ConfigurationParams configurationParams = configParamHandler.getConfigurationParamsInstance();
        Clusterer clusterer = new DBSCANCluster();
        try {
            configurationParams.parseParams(value.toString(), delimiter);
            clusterer.setup(configurationParams);
        } catch (ParsingException e) {
            throw new HdfsException(e);
        }

        List<List<DataObject>> sourceClusters = clusterer.createClusters(sourceImageData);
        List<List<DataObject>> targetClusters = clusterer.createClusters(targetImageData);

        Map<List<DataObject>, List<RigidTranformation>> map = new HashMap<>();

        for (List<DataObject> sourceCluster : sourceClusters) {
            List<RigidTranformation> list = new ArrayList<>();
            map.put(sourceCluster, list);
            for (List<DataObject> targetCluster : targetClusters) {
                RigidTranformation tranformation = computeCorrespondences(sourceCluster,
                                                                          targetCluster);
                if (tranformation.getCorrespondences().size() < 2)
                    continue;
                list.add(tranformation);
            }
        }

        for (Entry<List<DataObject>, List<RigidTranformation>> entry : map.entrySet()) {

            for (RigidTranformation transform : entry.getValue()) {
                TEXT_KEY.set(toString(transform.getCorrespondences()));
                context.write(TEXT_KEY, NullWritable.get());
            }
        }
    }

    private RigidTranformation computeCorrespondences(Collection<DataObject> sourceCluster,
                                                      Collection<DataObject> targetCluster) {

        // compute centroids as arithmetic mean of all points in the cluster
        DataObject sourceCentroid = new ImageData();
        sourceCentroid.setFeatures(DataTransformer.computeArithmeticMean(sourceCluster));

        DataObject targetCentroid = new ImageData();
        targetCentroid.setFeatures(DataTransformer.computeArithmeticMean(targetCluster));

        double[] angles = DataTransformer.computeDirectionAngles(sourceCentroid, targetCentroid);
        // FIXME: Place both images side-by-side and then compute distance
        double distance = DataTransformer.computeEuclideanDistance(sourceCentroid, targetCentroid);

        RigidTranformation transformation = new RigidTranformation(sourceCluster, targetCluster);
        transformation.setAngles(angles);
        transformation.setDistance(distance);

        // find relative position for all the points from the centroids and plot
        // in target space
        Map<DataObject, double[]> relativeMovementMap = relativePositions(sourceCluster,
                                                                          sourceCentroid);
        Map<DataObject, DataObject> translatedObjMapping = movePoints(targetCentroid,
                                                                      relativeMovementMap);

        // find correspondences between mapped source points and target points
        Set<DataCorrespondence> correspondences = findCorrespondences(translatedObjMapping,
                                                                      targetCluster);
        transformation.setCorrespondences(correspondences);
        return transformation;
    }

    private Map<DataObject, DataObject> movePoints(DataObject targetCenterPoint,
                                                   Map<DataObject, double[]> relativeMovementMap) {
        Map<DataObject, double[]> translatedPoints = DataTransformer.doTranslation(targetCenterPoint,
                                                                                   relativeMovementMap);

        Map<DataObject, DataObject> translatedObjMapping = new HashMap<>();
        for (Entry<DataObject, double[]> entry : translatedPoints.entrySet()) {
            DataObject translatedObj = new ImageData();
            translatedObj.setFeatures(entry.getValue());
            translatedObjMapping.put(entry.getKey(), translatedObj);
        }
        return translatedObjMapping;
    }

    private Set<DataCorrespondence> findCorrespondences(
                                                        Map<DataObject, DataObject> translatedObjMapping,
                                                        Collection<DataObject> targetCluster) {

        SortedMap<Double, List<DataCorrespondence>> sortedMap = findSortedCorrespondences(translatedObjMapping,
                                                                                          targetCluster);
        // create lookup for fast access
        Set<DataObject> sourceLookup = translatedObjMapping.keySet();
        Set<DataObject> targetLookup = new HashSet<>(targetCluster);

        Set<DataCorrespondence> correspondences = new HashSet<>();
        for (Entry<Double, List<DataCorrespondence>> entry : sortedMap.entrySet()) {
            List<DataCorrespondence> list = entry.getValue();
            for (DataCorrespondence correspondence : list) {
                // add correspondence if source and target points are not added
                if (sourceLookup.contains(correspondence.getSource())
                    && targetLookup.contains(correspondence.getTarget())) {

                    correspondences.add(correspondence);
                    // remove from lookup once added as correspondence
                    sourceLookup.remove(correspondence.getSource());
                    targetLookup.remove(correspondence.getTarget());
                    if (sourceLookup.isEmpty() || targetLookup.isEmpty())
                        return correspondences;
                }
            }
        }

        return correspondences;
    }

    private SortedMap<Double, List<DataCorrespondence>>
            findSortedCorrespondences(Map<DataObject, DataObject> translatedObjMapping,
                                      Collection<DataObject> targetCluster) {

        SortedMap<Double, List<DataCorrespondence>> sortedCorrespondences = new TreeMap<>();

        for (Entry<DataObject, DataObject> entry : translatedObjMapping.entrySet()) {
            DataObject sourcePoint = entry.getKey();
            DataObject movedPoint = entry.getValue();

            for (DataObject targetPoint : targetCluster) {
                double distance = DataTransformer.computeEuclideanDistance(movedPoint, targetPoint);
                if (distance > threshold)
                    continue;
                DataCorrespondence correspondence = new DataCorrespondence(sourcePoint,
                                                                           targetPoint);
                List<DataCorrespondence> list = sortedCorrespondences.get(distance);
                if (null == list) {
                    list = new ArrayList<>();
                    sortedCorrespondences.put(distance, list);
                }
                list.add(correspondence);
            }
        }
        return sortedCorrespondences;
    }

    private Map<DataObject, double[]> relativePositions(Collection<DataObject> cluster,
                                                        DataObject centroid) {
        Map<DataObject, double[]> relativeMovementMap = new HashMap<>();
        for (DataObject point : cluster) {
            double[] movement = DataTransformer.computeRelativePosition(centroid, point);
            relativeMovementMap.put(point, movement);
        }
        return relativeMovementMap;
    }

    private <T> String toString(Collection<T> points) {
        StringBuilder builder = new StringBuilder("[");
        for (T point : points) {
            builder.append(point).append("|");
        }
        builder.delete(builder.length() - 1, builder.length());
        return builder.append("]").toString();
    }

    public static void main(String[] args) {
        System.out.println("Hello world");
    }
}

class ClusterTranslation {

}

