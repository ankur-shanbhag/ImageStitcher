package neu.nctracer.data;

import java.util.Collection;
import java.util.Set;

public class RigidTranformation {

    private Collection<DataObject> sourceCluster;
    private Collection<DataObject> targetCluster;
    private double[] angles;
    private double distance;

    private Set<DataCorrespondence> correspondences;

    public RigidTranformation(Collection<DataObject> sourceCluster,
                              Collection<DataObject> targetCluster) {
        this.sourceCluster = sourceCluster;
        this.targetCluster = targetCluster;
    }

    public double[] getAngles() {
        return angles;
    }

    public void setAngles(double[] angles) {
        this.angles = angles;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Collection<DataObject> getSourceCluster() {
        return sourceCluster;
    }

    public Collection<DataObject> getTargetCluster() {
        return targetCluster;
    }

    public Set<DataCorrespondence> getCorrespondences() {
        return correspondences;
    }

    public void setCorrespondences(Set<DataCorrespondence> correspondences) {
        this.correspondences = correspondences;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sourceCluster == null) ? 0 : sourceCluster.hashCode());
        result = prime * result + ((targetCluster == null) ? 0 : targetCluster.hashCode());
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
        RigidTranformation other = (RigidTranformation) obj;
        if (sourceCluster == null) {
            if (other.sourceCluster != null)
                return false;
        } else if (!sourceCluster.equals(other.sourceCluster))
            return false;
        if (targetCluster == null) {
            if (other.targetCluster != null)
                return false;
        } else if (!targetCluster.equals(other.targetCluster))
            return false;
        return true;
    }
}
