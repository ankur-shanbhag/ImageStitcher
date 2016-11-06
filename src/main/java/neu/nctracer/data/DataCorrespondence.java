package neu.nctracer.data;

import neu.nctracer.exception.ParsingException;

/**
 * Class is used for holding one-to-one correspondence between two
 * {@linkplain DataObject}
 * 
 * @author Ankur Shanbhag
 *
 */
public class DataCorrespondence {

    private static final String CORRESPONDENCE_SEPARATOR = "#";
    private static final String FEATURES_SEPARATOR = ",";

    private DataObject source;
    private DataObject target;

    public DataCorrespondence(DataObject source, DataObject target) {
        this.source = source;
        this.target = target;
    }

    public void setCorrespondence(DataObject source, DataObject target) {
        this.source = source;
        this.target = target;
    }

    public DataObject getSource() {
        return source;
    }

    public DataObject getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 1;
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
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
        DataCorrespondence other = (DataCorrespondence) obj;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }

    /**
     * Returns String representation of this object
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (double feature : source.getFeatures())
            builder.append(feature).append(FEATURES_SEPARATOR);

        builder.replace(builder.length() - 1, builder.length(), CORRESPONDENCE_SEPARATOR);

        for (double feature : target.getFeatures())
            builder.append(feature).append(FEATURES_SEPARATOR);

        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    /**
     * Returns an object constructed by parsing the input data. To correctly get
     * back the object, the input data should be generated using
     * {@link DataCorrespondence#toString()}
     * 
     * @param data
     *            - toString representation of {@link DataCorrespondence}
     * @return
     * @throws ParsingException
     *             - if input data cannot be parsed - usually if data is not
     *             generated using {@link DataCorrespondence#toString()}
     */
    public static DataCorrespondence parse(String data) throws ParsingException {
        String[] split = data.split(CORRESPONDENCE_SEPARATOR);
        if (split.length != 2)
            throw new ParsingException("Cannot parse data input [" + data + "]");

        String[] features1 = split[0].split(FEATURES_SEPARATOR);
        String[] features2 = split[1].split(FEATURES_SEPARATOR);

        // TODO: Write class name along with data (toString)
        double[] sourceFeatures = new double[features1.length];
        try {
            for (int i = 0; i < features1.length; i++) {
                sourceFeatures[i] = Double.parseDouble(features1[i]);
            }
        } catch (NumberFormatException nfe) {
            throw new ParsingException("Incorrect data. Parsing failed [" + data + "]", nfe);
        }

        double[] targetFeatures = new double[features2.length];
        try {
            for (int i = 0; i < features2.length; i++) {
                targetFeatures[i] = Double.parseDouble(features2[i]);
            }
        } catch (NumberFormatException nfe) {
            throw new ParsingException("Incorrect data. Parsing failed [" + data + "]", nfe);
        }

        DataObject source = new ImageData();
        DataObject target = new ImageData();
        source.setFeatures(sourceFeatures);
        target.setFeatures(targetFeatures);

        return new DataCorrespondence(source, target);
    }
}

