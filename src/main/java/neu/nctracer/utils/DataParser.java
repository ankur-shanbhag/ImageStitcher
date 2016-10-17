package neu.nctracer.utils;

import java.util.ArrayList;
import java.util.Collection;

import neu.nctracer.data.DataObject;
import neu.nctracer.data.ImageData;
import neu.nctracer.exception.DataParsingException;

public final class DataParser {

    private static final String WHITE_SPACE_RECORD_SPLITTER = "\\s+";
    private static final String LINE_SPLITTER = "\\r?\\n";

    public static Collection<DataObject> parseImageData(String data) throws DataParsingException {
        return parseImageData(data, WHITE_SPACE_RECORD_SPLITTER);
    }

    public static Collection<DataObject> parseImageData(String data, String recordSplitter)
            throws DataParsingException {
        Collection<DataObject> parsedData = new ArrayList<>();
        String[] lines = data.split(LINE_SPLITTER);
        for (String line : lines) {
            try {
                String[] values = line.trim().split(recordSplitter);
                if (values != null) {

                    double[] features = new double[values.length];
                    for (int i = 0; i < values.length; i++) {
                        features[i] = Double.parseDouble(values[i]);
                    }

                    parsedData.add(new ImageData(features));
                }
            } catch (NumberFormatException exp) {
                throw new DataParsingException("Error while parsing record [" + line + "]", exp);
            }
        }
        return parsedData;
    }

    private DataParser() {
        // deny object creation
    }
}
