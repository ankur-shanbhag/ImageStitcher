package neu.nctracer.data;

import org.apache.commons.math3.ml.clustering.Clusterable;

/**
 * This interface specifies behaviors for all types of data object
 * 
 * @author Ankur Shanbhag
 */
public interface DataObject extends Comparable<DataObject>, Clusterable {

    void setFeatures(double[] features);

    int getDimension();

    double[] getFeatures();

    DataObject deepClone();

    int hashCode();

    boolean equals(Object obj);
}