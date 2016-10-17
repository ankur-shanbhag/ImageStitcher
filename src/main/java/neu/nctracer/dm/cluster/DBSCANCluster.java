package neu.nctracer.dm.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterer;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import neu.nctracer.data.DataObject;

/**
 * Implementation for DBSCAN algorithm for clustering the data points with
 * n-dimensional features using Euclidean distance method.
 * 
 * This class internally uses
 * <tt>org.apache.commons.math3.ml.clustering.DBSCANClusterer</tt> to form
 * density based clusters<br>
 * 
 * @see <a href=
 *      "https://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math3/ml/clustering/package-summary.html">
 *      DBSCANClusterer</a>
 *
 * 
 * @author Ankur Shanbhag
 * 
 */
public class DBSCANCluster implements neu.nctracer.dm.cluster.Clusterer {

    /**
     * maximum radius of the neighborhood
     */
    private final double eps;

    /**
     * Minimum number of points in Epsilon neighborhood to consider any point
     * for clustering
     */
    private final int minPoints;

    public DBSCANCluster(int minPoints, double eps) {
        this.minPoints = minPoints;

        this.eps = eps;
    }

    /**
     * Creates density based clusters for all the data points using DBSCAN
     * algorithm.<br>
     * Note: Some of the data points which do not form part of any cluster will
     * be considered as noise and hence ignored
     */
    @Override
    public List<List<DataObject>> createClusters(Collection<DataObject> dataPoints) {

        if (this.minPoints > dataPoints.size())
            throw new RuntimeException(
                    "MinPoints cannot have a value greater than total number of data points");

        Clusterer<DataObject> clustering = new DBSCANClusterer<>(this.eps, this.minPoints);

        List<? extends Cluster<DataObject>> dbscanClusters = clustering.cluster(dataPoints);

        List<List<DataObject>> clusters = new ArrayList<>();
        for (Cluster<DataObject> cluster : dbscanClusters) {
            clusters.add(cluster.getPoints());
        }

        return clusters;
    }

}