package neu.nctracer.dm.cluster;

import java.util.Collection;
import java.util.List;

import neu.nctracer.data.DataObject;
import neu.nctracer.dm.ConfigurationParams;
import neu.nctracer.exception.ParsingException;

/**
 * Contract for all classes implementing clustering algorithms
 * 
 * @author Ankur Shanbhag
 */
public interface Clusterer {

    void setup(ConfigurationParams params) throws ParsingException;

    List<DataCluster> createClusters(Collection<DataObject> dataPoints);
}
