package neu.nctracer.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class RigidTransformation implements DataObject {

    private Collection<DataObject> sourceCluster;
    private Collection<DataObject> targetCluster;
    private double[] angles;
    private double distance;

    private Set<DataCorrespondence> correspondences;

    public RigidTransformation(Collection<DataObject> sourceCluster,
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
        result = prime * result + Arrays.hashCode(angles);
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
        RigidTransformation other = (RigidTransformation) obj;
        if (!Arrays.equals(angles, other.angles))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(angles);
    }
    
    @Override
    public double[] getPoint() {
        return getAngles();
    }

    @Override
    public int compareTo(DataObject o) {
        throw new RuntimeException("Not implemented...");
    }

    @Override
    public void setFeatures(double[] features) {
        angles = features;

    }

    @Override
    public int getDimension() {
        return angles.length;
    }

    @Override
    public double[] getFeatures() {
        return angles;
    }
}
