package neu.nctracer.data;

import java.util.Arrays;

/**
 * Class to hold image data points in 3 dimensions
 * 
 * @author Ankur Shanbhag
 *
 */
public class ImageData implements DataObject {

    private double[] features;
    private int dimension;

    @Override
    public void setFeatures(double[] features) {
        this.features = features;
        this.dimension = features.length;
    }

    @Override
    public double[] getFeatures() {
        return features;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public double[] getPoint() {
        return getFeatures();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (double feature : features) {
            builder.append(feature).append(" ");
        }
        return builder.toString().trim();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dimension;
        result = prime * result + Arrays.hashCode(features);
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
        ImageData other = (ImageData) obj;
        if (dimension != other.dimension)
            return false;
        if (!Arrays.equals(features, other.features))
            return false;
        return true;
    }

    @Override
    public int compareTo(DataObject obj) {
        double[] features2 = obj.getFeatures();

        for (int i = 0; i < getDimension(); i++) {
            if (features[i] > features2[i])
                return 1;
            else if (features[i] < features2[i])
                return -1;
        }

        return 0;
    }

    @Override
    public ImageData deepClone() {
        ImageData clone = new ImageData();
        double[] clonedFeatures = Arrays.copyOf(this.getFeatures(), this.getDimension());
        clone.setFeatures(clonedFeatures);
        return clone;
    }

}