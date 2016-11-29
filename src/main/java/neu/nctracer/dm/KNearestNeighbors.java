package neu.nctracer.dm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.data.DataObject;
import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;
import neu.nctracer.utils.DataTransformer;

/**
 * Implementation for kNN algorithm for predicting the labels of data points
 * with n-dimensional features using Euclidean distance method
 * 
 * @author Ankur Shanbhag
 * 
 */
public class KNearestNeighbors implements NearestNeighbors {

    /**
     * Parameters that can be configured by the client
     */
    // value for maximum allowed distance
    public static final String PARAM_THRESHOLD_DISTANCE = "nearest.neighbor.threshold.distance";

    private List<DataObject> dataObjects;
    private double maxDistance;

    private Logger logger = LogManager.getLogManager().getDefaultLogger();

    @Override
    public void setup(List<DataObject> dataObjects, ConfigurationParams params) {
        this.dataObjects = dataObjects;

        String param = params.getParam(PARAM_THRESHOLD_DISTANCE);
        try {
            this.maxDistance = Double.parseDouble(param);
        } catch (Exception e) {
            // set to very large value, so that neighbors are not pruned
            this.maxDistance = Double.MAX_VALUE;
        }

        logger.debug("Setting maxDistance threshold to " + this.maxDistance);
    }

    /**
     * Finds K nearest neighbors to the input data object
     */
    @Override
    public Map<DataObject, Double> findNeighbors(DataObject object, final int K) {

        // get the distance for all the points from given data point
        SortedMap<Double, Set<DataObject>> kNNMap = getNeighborsByDistance(dataObjects, object);

        // loop-up to store labels for k nearest neighbors
        Map<DataObject, Double> neighbors = new LinkedHashMap<>();

        Iterator<Entry<Double, Set<DataObject>>> iterator = kNNMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<Double, Set<DataObject>> entry = iterator.next();

            // stop finding neighbors at certain distance
            if (entry.getKey() > maxDistance)
                return neighbors;

            for (DataObject neighbor : entry.getValue()) {
                neighbors.put(neighbor, entry.getKey());
                if (neighbors.size() == K)
                    return neighbors;
            }

        }

        return neighbors;
    }

    /**
     * Calculates distance between the specified data point and every point
     * specified in the training set
     * 
     * @param dataObjects
     * 
     * @param dataObjects
     *            - training set
     * @param DataObject
     *            - data point from which the distance needs to be calculated
     * @return map representing distance of the given data point from every data
     *         point in the training set
     */
    private static SortedMap<Double, Set<DataObject>>
            getNeighborsByDistance(List<? extends DataObject> dataObjects, DataObject object) {

        // map to store distance from every point in the given data set
        SortedMap<Double, Set<DataObject>> kNNMap = new TreeMap<>();

        for (DataObject neighbour : dataObjects) {
            // calculate distance from all the points one by one
            double distance = DataTransformer.computeEuclideanDistance(neighbour, object);

            // store them in the map. Map will also store multiple points which
            // are equi-distant from given data point
            Set<DataObject> neighbors = kNNMap.get(distance);
            if (null == neighbors) {
                Set<DataObject> set = new HashSet<>();
                set.add(neighbour);
                kNNMap.put(distance, set);
            } else {
                neighbors.add(neighbour);
            }
        }
        return kNNMap;
    }
}
