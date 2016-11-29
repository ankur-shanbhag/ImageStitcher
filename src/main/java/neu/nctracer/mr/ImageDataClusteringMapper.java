package neu.nctracer.mr;

import java.io.IOException;
import java.util.ArrayList;
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

import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.conf.cli.CLIConfigurationManager;
import neu.nctracer.data.DataCorrespondence;
import neu.nctracer.data.DataObject;
import neu.nctracer.data.DataTransformation;
import neu.nctracer.data.ImageData;
import neu.nctracer.data.Match;
import neu.nctracer.dm.cluster.Clusterer;
import neu.nctracer.dm.cluster.DBSCANCluster;
import neu.nctracer.dm.cluster.DataCluster;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.exception.ParsingException;
import neu.nctracer.exception.ReflectionUtilsException;
import neu.nctracer.utils.DataTransformer;
import neu.nctracer.utils.ReflectionUtils;

/**
 * Mapper class to run image stitching algorithm on 2 image stacks (namely
 * source and target) and find one-to-one correspondence of matching points (if
 * any) in these image stacks.<br>
 * 
 * Algorithm implementation details:<br>
 * 1. Read source and target image data points from HDFS<br>
 * 2. Find clusters of data points using configurations specified in the input
 * line to the mapper. DBSCAN used as default clustering algorithm<br>
 * 3. Find cluster center as arithmetic mean of all points in the cluster<br>
 * 4. Define translation from source cluster center point to a target cluster
 * center point. Apply this translation to all points in the source cluster<br>
 * 6. Find one-to-one correspondence between translated source cluster points
 * and target cluster points. Choose closest target point (using euclidean
 * distance) as correspondence. Compute error.<br>
 * 7. Performs steps 3 through 6 for all pairs of source and target cluster
 * points.<br>
 * 8. For all source clusters, pick target clusters and translation defined
 * above. Find group of translations which are consistent across different pairs
 * picked. Compute translation error for all cluster pairs in a group. Repeat
 * again for different pairs of source and target clusters. Keep track of
 * minimum error and associated group.<br>
 * 9. For the group with minimum error, emit one-to-one correspondences from
 * cluster pairs.
 * 
 * @author Ankur Shanbhag
 *
 */
public class ImageDataClusteringMapper
        extends ImageStitchingMapper<LongWritable, Text, Text, NullWritable> {

    private double threshold = 0.0;

    private final Text TEXT_KEY = new Text();

    private CLIConfigurationManager configParamHandler = CLIConfigurationManager.getHandler();

    private Clusterer clusterer = null;

    @Override
    protected void
              setup(Mapper<LongWritable, Text, Text, NullWritable>.Context context) throws IOException,
                                                                                    InterruptedException {
        super.setup(context);

        Configuration conf = context.getConfiguration();

        String clustererClassName = conf.get("image.matching.cluster.name",
                                             DBSCANCluster.class.getName());
        try {
            clusterer = ReflectionUtils.instantiate(clustererClassName, Clusterer.class);
            logger.debug("Using class ["
                         + clusterer.getClass().getName()
                         + "] for clustering data objects.");
        } catch (ReflectionUtilsException e) {
            throw new RuntimeException(e);
        }

        threshold = conf.getDouble(HdfsConstants.IMAGE_MATCHING_ERROR, 0.0);

        logger.info("Image stitching mapreduce job setup phase successful.");
    }

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException,
                                                                   InterruptedException {
        Configuration conf = context.getConfiguration();
        String delimiter = conf.get("image.matching.cluster.params.delimiter", "\\s*,\\s*");

        ConfigurationParams configurationParams = configParamHandler.getConfigurationParamsInstance();
        try {
            configurationParams.parseParams(value.toString(), delimiter);
            clusterer.setup(configurationParams);
        } catch (ParsingException e) {
            logger.error("Error while parsing configuration params : " + value.toString(), e);
            throw new HdfsException(e);
        }

        List<DataCluster> sourceClusters = clusterer.createClusters(getSourceDataObjects());
        List<DataCluster> targetClusters = clusterer.createClusters(getTargetDataObjects());

        List<List<DataTransformation<DataCluster>>> allTransformations = new ArrayList<>();
        Map<DataTransformation<DataCluster>, Match> resultMap = new HashMap<>();

        for (DataCluster sourceCluster : sourceClusters) {
            List<DataTransformation<DataCluster>> list = new ArrayList<>();
            for (DataCluster targetCluster : targetClusters) {

                DataTransformation<DataCluster> transformation = defineTranslation(sourceCluster,
                                                                                   targetCluster);
                Match match = computeCorrespondences(sourceCluster, targetCluster);

                resultMap.put(transformation, match);
                if (match.getCorrespondences().size() > 1)
                    list.add(transformation);
            }
            if (!list.isEmpty())
                allTransformations.add(list);
        }

        if (allTransformations.size() < 2)
            return;

        List<DataObject> transformations = groupTransformations(allTransformations);
        emitCorrespondences(context, transformations, resultMap);

        logger.info("Successfully performed map phase for input : " + value);
    }

    private List<DataObject>
            groupTransformations(List<List<DataTransformation<DataCluster>>> transformations) {
        Map<DataCluster, DataObject> targetLookup = new HashMap<>();
        List<DataObject> clusterList = new ArrayList<>();
        groupTransformations(transformations, targetLookup, 0, clusterList);

        return clusterList;
    }

    private void groupTransformations(List<List<DataTransformation<DataCluster>>> transformations,
                                      Map<DataCluster, DataObject> targetLookup,
                                      int num,
                                      List<DataObject> clusterList) {

        if (num == transformations.size()) {
            if (targetLookup.size() < 2)
                return;

            ConfigurationParams params = configParamHandler.getConfigurationParamsInstance();
            params.setParam("minpoints", "2");
            params.setParam("eps", "20");

            clusterer.setup(params);
            List<DataCluster> clusters = clusterer.createClusters(targetLookup.values());

            List<DataObject> list = findOptimum(clusters, clusterList);
            if (list != clusterList) {
                clusterList.clear();
                clusterList.addAll(list);
            }
            return;
        }

        List<DataTransformation<DataCluster>> list = transformations.get(num);
        for (int i = 0; i < list.size(); i++) {
            DataTransformation<DataCluster> transformation = list.get(i);
            if (targetLookup.containsKey(transformation.getTargetObject()))
                continue;
            targetLookup.put(transformation.getTargetObject(), transformation);
            groupTransformations(transformations, targetLookup, num + 1, clusterList);
            targetLookup.remove(transformation.getTargetObject());
        }

        // do not add any mapping for this source
        groupTransformations(transformations, targetLookup, num + 1, clusterList);
    }

    private List<DataObject> findOptimum(List<DataCluster> clusters, List<DataObject> clusterList) {

        double minErr = Double.MAX_VALUE;
        DataCluster optimum = null;

        for (DataCluster cluster : clusters) {
            double[] center = DataTransformer.computeArithmeticMean(cluster.getDataPoints());
            double err = 0;
            for (DataObject transformation : cluster.getDataPoints()) {
                double[] features = transformation.getFeatures();
                for (int i = 0; i < center.length; i++) {
                    err += Math.abs(center[i] - features[i]);
                }
            }

            if (err < minErr) {
                optimum = cluster;
                minErr = err;
            }
        }

        if (clusterList.isEmpty())
            return optimum.getDataPoints();

        double[] center = DataTransformer.computeArithmeticMean(clusterList);
        double err = 0;
        for (DataObject transformation : clusterList) {
            double[] features = transformation.getFeatures();
            for (int i = 0; i < center.length; i++) {
                err += Math.abs(center[i] - features[i]);
            }
        }

        return (null == optimum || err < minErr) ? clusterList : optimum.getDataPoints();
    }

    private Match computeCorrespondences(DataCluster sourceCluster, DataCluster targetCluster) {

        // compute centroids as arithmetic mean of all points in the
        // cluster
        DataObject sourceCentroid = new ImageData();
        sourceCentroid.setFeatures(DataTransformer.computeArithmeticMean(sourceCluster.getDataPoints()));

        DataObject targetCentroid = new ImageData();
        targetCentroid.setFeatures(DataTransformer.computeArithmeticMean(targetCluster.getDataPoints()));

        // find relative position for all the points from the centroids and plot
        // in target space
        Map<DataObject, double[]> relativeMovementMap = relativePositions(sourceCluster,
                                                                          sourceCentroid);
        Map<DataObject, DataObject> translatedObjMapping = movePoints(targetCentroid,
                                                                      relativeMovementMap);

        // find correspondences between mapped source points and target points
        Set<DataCorrespondence> correspondences = findCorrespondences(translatedObjMapping,
                                                                      targetCluster);
        Match result = new Match(0.0, correspondences);
        return result;
    }

    /**
     * Define translation between 2 input clusters based on the center points
     */
    private DataTransformation<DataCluster> defineTranslation(DataCluster sourceCluster,
                                                              DataCluster targetCluster) {

        // compute centroids as arithmetic mean of all points in the
        // cluster
        DataObject sourceCentroid = new ImageData();
        sourceCentroid.setFeatures(DataTransformer.computeArithmeticMean(sourceCluster.getDataPoints()));

        DataObject targetCentroid = new ImageData();
        targetCentroid.setFeatures(DataTransformer.computeArithmeticMean(targetCluster.getDataPoints()));

        // define translation from source to target
        double[] angles = DataTransformer.computeDirectionAngles(sourceCentroid, targetCentroid);
        // FIXME: Place both images side-by-side and then compute distance
        double distance = DataTransformer.computeEuclideanDistance(sourceCentroid, targetCentroid);

        DataTransformation<DataCluster> transformation = new DataTransformation<>();
        transformation.setTranslationObjects(sourceCluster, targetCluster);
        transformation.setAngles(angles);
        transformation.setDistance(distance);
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
                                                        DataCluster targetCluster) {

        SortedMap<Double, List<DataCorrespondence>> sortedMap = findSortedCorrespondences(translatedObjMapping,
                                                                                          targetCluster);
        // create lookup for fast access
        Set<DataObject> sourceLookup = translatedObjMapping.keySet();
        Set<DataObject> targetLookup = new HashSet<>(targetCluster.getDataPoints());

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
                                      DataCluster targetCluster) {

        SortedMap<Double, List<DataCorrespondence>> sortedCorrespondences = new TreeMap<>();

        for (Entry<DataObject, DataObject> entry : translatedObjMapping.entrySet()) {
            DataObject sourcePoint = entry.getKey();
            DataObject movedPoint = entry.getValue();

            for (DataObject targetPoint : targetCluster.getDataPoints()) {
                double distance = DataTransformer.computeEuclideanDistance(movedPoint, targetPoint);
                if (distance > threshold)
                    continue;
                DataCorrespondence correspondence = new DataCorrespondence(sourcePoint,
                                                                           targetPoint,
                                                                           distance);
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

    private Map<DataObject, double[]> relativePositions(DataCluster cluster, DataObject centroid) {
        Map<DataObject, double[]> relativeMovementMap = new HashMap<>();
        for (DataObject point : cluster.getDataPoints()) {
            double[] movement = DataTransformer.computeRelativePosition(centroid, point);
            relativeMovementMap.put(point, movement);
        }
        return relativeMovementMap;
    }

    private void
            emitCorrespondences(Context context,
                                List<DataObject> transformations,
                                Map<DataTransformation<DataCluster>, Match> resultMap) throws IOException,
                                                                                       InterruptedException {
        for (DataObject transform : transformations) {
            if (transform instanceof DataTransformation) {
                Match match = resultMap.get(transform);
                Set<DataCorrespondence> correspondences = match.getCorrespondences();
                for (DataCorrespondence correspondence : correspondences) {
                    TEXT_KEY.set(correspondence.toString());
                    context.write(TEXT_KEY, NullWritable.get());
                }
            }
        }
    }
}
