package neu.nctracer.dm.cluster;

import java.util.Collection;
import java.util.List;

import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.data.DataObject;

/**
 * Contract for all classes implementing clustering algorithms
 * 
 * @author Ankur Shanbhag
 */
public interface Clusterer {

    /**
     * Parameters to be required by the clustering algorithm
     * 
     * @param params
     * @throws IllegalArgumentException
     *             the algorithm may choose to throw this exception if required
     *             parameters are missing and it cannot use default values
     *             instead
     */
    void setup(ConfigurationParams params) throws IllegalArgumentException;

    List<DataCluster> createClusters(Collection<DataObject> dataPoints);
}
