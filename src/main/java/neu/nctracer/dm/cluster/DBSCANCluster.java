package neu.nctracer.dm.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import neu.nctracer.data.DataObject;
import neu.nctracer.dm.conf.ConfigurationParams;

/**
 * Implementation for DBSCAN algorithm for clustering the data points with
 * n-dimensional features using Euclidean distance method.
 * 
 * This class internally uses
 * <tt>org.apache.commons.math3.ml.clustering.DBSCANClusterer</tt> to form
 * density based clusters<br>
 * 
 * @see <a>
 *      https://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/ml/clustering/package-summary.html
 *      </a>
 *
 * 
 * @author Ankur Shanbhag
 * 
 */
public class DBSCANCluster implements Clusterer {

    /**
     * maximum radius of the neighborhood
     */
    private double eps;

    /**
     * Minimum number of points in Epsilon neighborhood to consider any point
     * for clustering
     */
    private int minPoints;

    @Override
    public void setup(ConfigurationParams params) throws IllegalArgumentException {
        try {
            this.minPoints = Integer.parseInt(params.getParam("minpoints"));
            this.eps = Double.parseDouble(params.getParam("eps"));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Error parsing params [minpoints, eps]. Configuration parameters received "
                                               + params.toString(),
                                               nfe);
        }
    }

    /**
     * Creates density based clusters for all the data points using DBSCAN
     * algorithm.<br>
     * Note: Some of the data points which do not form part of any cluster will
     * be considered as noise and hence ignored
     */
    @Override
    public List<DataCluster> createClusters(Collection<DataObject> dataPoints) {
        if (this.minPoints > dataPoints.size())
            throw new RuntimeException("Too few data points to perform DBSCAN clustering");

        org.apache.commons.math3.ml.clustering.Clusterer<DataObject> clustering = null;

        clustering = new DBSCANClusterer<>(this.eps, this.minPoints);

        List<? extends Cluster<DataObject>> dbscanClusters = clustering.cluster(dataPoints);

        List<DataCluster> clusters = new ArrayList<>();
        for (Cluster<DataObject> cluster : dbscanClusters) {
            clusters.add(new DataCluster(cluster.getPoints()));
        }

        return clusters;
    }
}
