package neu.nctracer.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import neu.nctracer.data.DataObject;

/**
 * Class to perform statistical analysis such mean, distance etc. on
 * {@link DataObject}
 * 
 * @author Ankur Shanbhag
 *
 */
public class DataTransformer {

    public static double[] computeArithmeticMean(Collection<DataObject> points) {
        if (null == points)
            return null;

        Iterator<DataObject> iterator = points.iterator();

        double[] arithmeticMean = null;

        while (iterator.hasNext()) {
            DataObject point = iterator.next();
            double[] features = point.getFeatures();

            if (null == arithmeticMean)
                arithmeticMean = new double[features.length];

            for (int i = 0; i < features.length; i++) {
                arithmeticMean[i] += features[i];
            }
        }

        for (int i = 0; i < arithmeticMean.length; i++) {
            arithmeticMean[i] /= points.size();
        }

        return arithmeticMean;
    }

    public static double[] computeEuclideanDistance(DataObject point,
                                                    Collection<DataObject> points) {

        double[] distances = new double[points.size()];
        int i = 0;
        for (DataObject otherPoint : points) {
            distances[i++] = computeEuclideanDistance(point, otherPoint);
        }

        return distances;
    }

    public static double computeEuclideanDistance(DataObject point1, DataObject point2) {

        double[] features1 = point1.getFeatures();
        double[] features2 = point2.getFeatures();

        double distance = 0.0;
        for (int i = 0; i < features1.length; i++) {
            // add square of differences
            distance += Math.pow(features1[i] - features2[i], 2);
        }

        // square root to get euclidean distance
        return Math.sqrt(distance);
    }

    public static double[] computeDirectionAngles(DataObject point1, DataObject point2) {
        double distance = computeEuclideanDistance(point1, point2);

        double[] features1 = point1.getFeatures();
        double[] features2 = point2.getFeatures();

        double angles[] = new double[point1.getDimension()];
        for (int i = 0; i < point1.getDimension(); i++) {
            // diff of values for each axis (x,y,z)
            double axis = features2[i] - features1[i];
            double angleInRadians = Math.acos(axis / distance);

            angles[i] = Math.toDegrees(angleInRadians);
        }
        return angles;
    }

    public static double[] computeRelativePosition(DataObject point1, DataObject point2) {
        double[] movement = new double[point1.getDimension()];
        double[] features1 = point1.getFeatures();
        double[] features2 = point2.getFeatures();
        for (int i = 0; i < point1.getDimension(); i++) {
            movement[i] = features1[i] - features2[i];
        }
        return movement;
    }

    public static Map<DataObject, double[]>
           doTranslation(DataObject anchorPoint, Map<DataObject, double[]> relativeMovementMap) {
        Map<DataObject, double[]> movedObjects = new HashMap<>();
        for (Entry<DataObject, double[]> entry : relativeMovementMap.entrySet()) {
            DataObject point = entry.getKey();
            movedObjects.put(point, doTranslation(anchorPoint, entry.getValue()));
        }
        return movedObjects;
    }

    public static double[] doTranslation(DataObject point, double[] movement) {
        double translatedObj[] = new double[point.getDimension()];
        double[] features = point.getFeatures();
        for (int i = 0; i < point.getDimension(); i++) {
            translatedObj[i] = features[i] + movement[i];
        }
        return translatedObj;
    }

    public static double[] computeMinValues(Collection<DataObject> dataObjects) {
        double[] minValues = null;
        Iterator<DataObject> iterator = dataObjects.iterator();
        while (iterator.hasNext()) {
            DataObject dataObject = iterator.next();
            if (null == minValues) {
                minValues = new double[dataObject.getDimension()];
                // initialize to very large value
                for (int i = 0; i < minValues.length; i++) {
                    minValues[i] = Double.MAX_VALUE;
                }
            }

            double[] features = dataObject.getFeatures();
            for (int i = 0; i < minValues.length; i++) {
                if (minValues[i] > features[i])
                    minValues[i] = features[i];
            }
        }

        return minValues;
    }

    private DataTransformer() {
        // Deny object creation
    }
}

