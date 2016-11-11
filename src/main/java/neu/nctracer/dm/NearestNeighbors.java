package neu.nctracer.dm;

import java.util.List;
import java.util.Map;

import neu.nctracer.data.DataObject;
import neu.nctracer.dm.conf.ConfigurationParams;

public interface NearestNeighbors {

    void setup(List<DataObject> dataObjects, ConfigurationParams params);

    Map<DataObject, Double> findNeighbors(DataObject object, final int K);
}
