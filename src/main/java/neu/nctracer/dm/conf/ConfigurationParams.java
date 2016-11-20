package neu.nctracer.dm.conf;

import java.util.Map;

import neu.nctracer.exception.ParsingException;

/**
 * This interface defines behaviors to dynamically parse/set/get configuration
 * parameters. These parameters can be used to pass dependencies using common
 * interface to all the classes
 * 
 * @author Ankur Shanbhag
 *
 */
public interface ConfigurationParams {

    void parseParams(String paramStr, String delimiter) throws ParsingException;

    void parseParams(String[] params) throws ParsingException;

    void setParams(Map<String, String> params);

    void setParam(String name, String value);

    String getParam(String name);

    String getParam(String name, String defaultValue);

    Map<String, String> getParams();
}

