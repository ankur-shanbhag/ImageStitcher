package neu.nctracer.utils;

import java.util.Collection;
import java.util.Iterator;

import neu.nctracer.data.DataObject;
import neu.nctracer.exception.ConfigurationException;

/**
 * Class to perform statistical analysis such mean, distance etc. on
 * {@link DataObject}
 * 
 * @author Ankur Shanbhag
 *
 */
public class DataAnalyser {

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

    private DataAnalyser() {
    }

    public static void main(String args[]) throws ConfigurationException {

        // System.out.println("Hello world");
        // double[] arr1 = { 2, 4, 4 };
        // double[] arr2 = { 1, 6, 2 };
        //
        // System.out.println(Arrays
        // .toString(computeDirectionAngles(new ImageData(arr1), new
        // ImageData(arr2))));
        //
        // System.out.println(Arrays
        // .toString(computeDirectionAngles(new ImageData(arr2), new
        // ImageData(arr1))));

    }
}
