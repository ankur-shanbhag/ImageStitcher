package neu.nctracer.dm;

import java.util.Map;

import neu.nctracer.exception.ParsingException;

public interface ConfigurationParams {

    void parseParams(String paramStr, String delimiter) throws ParsingException;

    void setParams(Map<String, String> params);

    String getParam(String name);
}
