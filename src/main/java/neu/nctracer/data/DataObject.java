package neu.nctracer.data;

import org.apache.commons.math3.ml.clustering.Clusterable;

public interface DataObject extends Comparable<DataObject>, Clusterable {

    int getDimension();

    double[] getFeatures();

}
