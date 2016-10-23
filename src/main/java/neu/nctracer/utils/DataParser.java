package neu.nctracer.utils;

import java.util.ArrayList;
import java.util.Collection;

import neu.nctracer.data.DataObject;
import neu.nctracer.exception.ParsingException;

public final class DataParser {

    private static final String WHITE_SPACE_RECORD_SPLITTER = "\\s+";
    private static final String LINE_SPLITTER = "\\r?\\n";

    public static Collection<DataObject> parseImageData(String data,
                                                        Class<? extends DataObject> clazz)
                                                        throws ParsingException {
        return parseImageData(data, WHITE_SPACE_RECORD_SPLITTER, clazz);
    }

    public static Collection<DataObject> parseImageData(String data,
                                                        String recordSplitter,
                                                        Class<? extends DataObject> clazz)
                                                        throws ParsingException {
        Collection<DataObject> parsedData = new ArrayList<>();
        String[] lines = data.split(LINE_SPLITTER);
        for (String line : lines) {
            try {
                String[] values = line.trim().split(recordSplitter);
                if (values != null) {
                    double[] features = new double[values.length - 1];
                    for (int i = 0; i < values.length - 1; i++) {
                        features[i] = Double.parseDouble(values[i]);
                    }

                    DataObject dataObj = clazz.newInstance();
                    dataObj.setFeatures(features);
                    parsedData.add(dataObj);
                }
            } catch (NumberFormatException exp) {
                throw new ParsingException("Error while parsing record [" + line + "]", exp);
            } catch (InstantiationException e) {
                throw new ParsingException(
                        "Error instantiating class [" + clazz.getName() + "]", e);
            } catch (IllegalAccessException e) {
                throw new ParsingException(
                        "Error instantiating class [" + clazz.getName() + "]", e);
            }
        }
        return parsedData;
    }

    private DataParser() {
        // deny object creation
    }
}

