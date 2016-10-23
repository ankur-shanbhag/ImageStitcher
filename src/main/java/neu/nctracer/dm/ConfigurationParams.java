package neu.nctracer.dm;

import neu.nctracer.exception.ParsingException;

public interface ConfigurationParams {

    void parseParams(String paramStr, String delimiter) throws ParsingException;

    String getParam(String name);
}
