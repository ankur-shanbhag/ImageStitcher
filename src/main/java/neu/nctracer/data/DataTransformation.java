package neu.nctracer.data;

import java.util.Arrays;

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
        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(angles);
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
}

