package neu.nctracer.dm.cluster;

import java.util.List;
import java.util.UUID;

import neu.nctracer.data.DataObject;

/**
 * Represents group of points with associated group id to uniquely identify the
 * group
 * 
 * @author Ankur Shanbhag
 */
public class DataCluster implements Comparable<DataCluster> {

    private final String clusterId;
    private final List<DataObject> cluster;

    /**
     * Generates a unique ID for the cluster
     * 
     * @param cluster
     */
    public DataCluster(List<DataObject> cluster) {
        clusterId = UUID.randomUUID().toString();
        this.cluster = cluster;
    }

    public DataCluster(String clusterId, List<DataObject> cluster) {
        this.clusterId = clusterId;
        this.cluster = cluster;
    }

    public List<DataObject> getDataPoints() {
        return cluster;
    }

    public String getClusterId() {
        return clusterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clusterId == null) ? 0 : clusterId.hashCode());
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
        DataCluster other = (DataCluster) obj;
        if (clusterId == null) {
            if (other.clusterId != null)
                return false;
        } else if (!clusterId.equals(other.clusterId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Cluster ID - " + clusterId + ", Cluster - " + cluster;
    }

    @Override
    public int compareTo(DataCluster o) {
        throw new RuntimeException("Not yet implemented ...");
    }
}

