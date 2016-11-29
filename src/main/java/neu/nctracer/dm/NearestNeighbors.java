package neu.nctracer.dm;

import java.util.List;
import java.util.Map;

import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.data.DataObject;

public interface NearestNeighbors {

    void setup(List<DataObject> dataObjects, ConfigurationParams params);

    Map<DataObject, Double> findNeighbors(DataObject object, final int K);
}
