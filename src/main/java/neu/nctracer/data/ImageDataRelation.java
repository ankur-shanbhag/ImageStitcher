package neu.nctracer.data;

import java.util.Arrays;

import neu.nctracer.utils.DataAnalyser;

public class ImageDataRelation implements DataObjectRelation {

    private DataObject point1;
    private DataObject point2;
    private double[] angles;
    private double distance;

    @Override
    public void setDataObjects(DataObject point1, DataObject point2) {
        this.point1 = point1;
        this.point2 = point2;
    }

    @Override
    public void computeRelation() {
        this.distance = DataAnalyser.computeEuclideanDistance(point1, point2);
        this.angles = DataAnalyser.computeDirectionAngles(point1, point2);
    }

    public DataObject getPoint1() {
        return this.point1;
    }

    public DataObject getPoint2() {
        return this.point2;
    }

    @Override
    public double getDistance() {
        return this.distance;
    }

    @Override
    public double[] getAngles() {
        return this.angles;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(angles);
        long temp;
        temp = Double.doubleToLongBits(distance);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((point1 == null) ? 0 : point1.hashCode());
        result = prime * result + ((point2 == null) ? 0 : point2.hashCode());
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
        ImageDataRelation other = (ImageDataRelation) obj;
        if (!Arrays.equals(angles, other.angles))
            return false;
        if (Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance))
            return false;
        if (point1 == null) {
            if (other.point1 != null)
                return false;
        } else if (!point1.equals(other.point1))
            return false;
        if (point2 == null) {
            if (other.point2 != null)
                return false;
        } else if (!point2.equals(other.point2))
            return false;
        return true;
    }

    @Override
    public int compareTo(DataObjectRelation obj) {
        throw new RuntimeException("Not yet implemented ...");
    }
}
