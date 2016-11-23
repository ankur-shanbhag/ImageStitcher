package neu.nctracer.data;

import java.util.Arrays;

import neu.nctracer.exception.ParsingException;

/**
 * Defines translation between entities defined by parameters of type
 * <code>T</code>
 * 
 * @author Ankur Shanbhag
 *
 * @param <T>
 *            - defines translation between type of elements
 */
public class DataTransformation<T> implements DataObject {

    private static final String COMPONENT_SEPARATOR = "#";
    private static final String FEATURES_SEPARATOR = ",";

    private T sourceObj;
    private T targetObj;
    private double[] angles;
    private double distance;

    public void setTranslationObjects(T sourceObj, T targetObj) {
        this.sourceObj = sourceObj;
        this.targetObj = targetObj;
    }

    public T getSourceObject() {
        return sourceObj;
    }

    public T getTargetObject() {
        return targetObj;
    }

    public double[] getAngles() {
        return angles;
    }

    public void setAngles(double[] angles) {
        this.angles = angles;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(angles);
        long temp = Double.doubleToLongBits(distance);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataTransformation other = (DataTransformation) obj;
        if (!Arrays.equals(angles, other.angles))
            return false;
        if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(distance).append(COMPONENT_SEPARATOR);

        for (double angle : angles) {
            builder.append(angle).append(FEATURES_SEPARATOR);
        }
        builder.delete(builder.length() - 1, builder.length());

        return builder.toString();
    }

    @Override
    public double[] getPoint() {
        return getAngles();
    }

    @Override
    public int compareTo(DataObject o) {
        throw new RuntimeException("Not implemented...");
    }

    @Override
    public void setFeatures(double[] features) {
        setAngles(features);
    }

    @Override
    public int getDimension() {
        return angles.length;
    }

    @Override
    public double[] getFeatures() {
        return getAngles();
    }

    @Override
    public DataObject deepClone() {
        throw new RuntimeException("Not implemented...");
    }

    public static DataTransformation parse(String data) throws ParsingException {
        String[] split = data.split(COMPONENT_SEPARATOR);
        if (split.length != 2)
            throw new ParsingException("Cannot parse data input [" + data + "]");

        double distance = parseDistance(data, split);
        double[] angles = parseAngles(split[1]);

        DataTransformation tranformation = new DataTransformation();
        tranformation.setDistance(distance);
        tranformation.setAngles(angles);
        return tranformation;
    }

    private static double parseDistance(String data, String[] split) throws ParsingException {
        try {
            return Double.parseDouble(split[0]);
        } catch (NumberFormatException nfe) {
            throw new ParsingException("Incorrect data. Parsing failed [" + data + "]", nfe);
        }
    }

    private static double[] parseAngles(String data) throws ParsingException {
        String[] split = data.split(FEATURES_SEPARATOR);
        if (split.length != 3)
            throw new ParsingException("Cannot parse data input [" + data + "]");

        double angles[] = new double[3];
        try {
            for (int i = 0; i < angles.length; i++)
                angles[i] = Double.parseDouble(split[i]);
        } catch (NumberFormatException nfe) {
            throw new ParsingException("Cannot parse data input [" + data + "]");
        }

        return angles;
    }
}

