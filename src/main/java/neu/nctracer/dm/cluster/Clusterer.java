package neu.nctracer.dm.cluster;

import java.util.Collection;
import java.util.List;

import neu.nctracer.data.DataObject;

public interface Clusterer {

    List<List<DataObject>> createClusters(Collection<DataObject> dataPoints);
}
