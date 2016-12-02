package neu.nctracer.data;

import neu.nctracer.exception.ParsingException;

/**
 * Class is used for holding one-to-one correspondence between two
 * {@linkplain DataObject}
 * 
 * @author Ankur Shanbhag
 *
 */
public class DataCorrespondence implements Comparable<DataCorrespondence> {

    private static final String COMPONENT_SEPARATOR = "#";
    private static final String FEATURES_SEPARATOR = ",";

    private DataObject source;
    private DataObject target;
    private DataObject translatedSource;
    private double error;

    public DataCorrespondence(DataObject source,
                              DataObject translatedSource,
                              DataObject target,
                              double error) {
        this.source = source;
        this.translatedSource = translatedSource;
        this.target = target;
        this.error = error;
    }

    public double getError() {
        return error;
    }

    public DataObject getSource() {
        return source;
    }

    public DataObject getTranslatedSource() {
        return translatedSource;
    }

    public DataObject getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(error);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        result = prime * result + ((translatedSource == null) ? 0 : translatedSource.hashCode());
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
        if (Double.doubleToLongBits(error) != Double.doubleToLongBits(other.error))
            return false;
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
        if (translatedSource == null) {
            if (other.translatedSource != null)
                return false;
        } else if (!translatedSource.equals(other.translatedSource))
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

        builder.replace(builder.length() - 1, builder.length(), COMPONENT_SEPARATOR);

        for (double feature : translatedSource.getFeatures())
            builder.append(feature).append(FEATURES_SEPARATOR);

        builder.replace(builder.length() - 1, builder.length(), COMPONENT_SEPARATOR);

        for (double feature : target.getFeatures())
            builder.append(feature).append(FEATURES_SEPARATOR);

        builder.replace(builder.length() - 1, builder.length(), COMPONENT_SEPARATOR);
        builder.append(error);

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
        String[] split = data.split(COMPONENT_SEPARATOR);
        if (split.length != 4)
            throw new ParsingException("Cannot parse data input [" + data + "]");

        String[] features1 = split[0].split(FEATURES_SEPARATOR);
        String[] features2 = split[1].split(FEATURES_SEPARATOR);
        String[] features3 = split[2].split(FEATURES_SEPARATOR);
        String errorStr = split[3];

        // TODO: Write class name along with data (toString)
        DataObject source = parseDataObject(data, features1);
        DataObject translatedSource = parseDataObject(data, features2);
        DataObject target = parseDataObject(data, features3);
        double error = parseError(data, errorStr);

        return new DataCorrespondence(source, translatedSource, target, error);
    }

    private static DataObject parseDataObject(String data,
                                              String[] features) throws ParsingException {
        double[] parsedFeatures = new double[features.length];
        try {
            for (int i = 0; i < features.length; i++) {
                parsedFeatures[i] = Double.parseDouble(features[i]);
            }
        } catch (NumberFormatException nfe) {
            throw new ParsingException("Incorrect data. Parsing failed [" + data + "]", nfe);
        }

        DataObject obj = new ImageData();
        obj.setFeatures(parsedFeatures);
        return obj;
    }

    private static double parseError(String data, String errorStr) throws ParsingException {
        try {
            return Double.parseDouble(errorStr);
        } catch (NumberFormatException nfe) {
            throw new ParsingException("Incorrect data. Parsing failed [" + data + "]", nfe);
        }
    }

    /**
     * Error based comparison
     */
    @Override
    public int compareTo(DataCorrespondence other) {
        return Double.valueOf(this.getError()).compareTo(Double.valueOf(other.getError()));
    }
}

