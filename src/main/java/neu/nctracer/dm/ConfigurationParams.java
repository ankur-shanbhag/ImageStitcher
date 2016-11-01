package neu.nctracer.dm;

import java.util.Map;

import neu.nctracer.exception.ParsingException;

/**
 * This interface defines behaviors to dynamically set/get configuration
 * parameters required by all the data mining algorithms
 * 
 * @author Ankur Shanbhag
 *
 */
public interface ConfigurationParams {

    void parseParams(String paramStr, String delimiter) throws ParsingException;

    void setParams(Map<String, String> params);

    String getParam(String name);
}
