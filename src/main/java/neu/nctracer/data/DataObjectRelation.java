package neu.nctracer.data;

public interface DataObjectRelation extends Comparable<DataObjectRelation> {
    void setDataObjects(DataObject point1, DataObject point2);

    void computeRelation();

    double getDistance();

    double[] getAngles();
}
