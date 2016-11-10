package neu.nctracer.data;

import java.util.Set;

public class Match {

    private Set<DataCorrespondence> correspondences;
    private double error;

    public Set<DataCorrespondence> getCorrespondences() {
        return correspondences;
    }

    public void setCorrespondences(Set<DataCorrespondence> correspondences) {
        this.correspondences = correspondences;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(error);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        Match other = (Match) obj;
        if (Double.doubleToLongBits(error) != Double.doubleToLongBits(other.error))
            return false;
        return true;
    }
}
