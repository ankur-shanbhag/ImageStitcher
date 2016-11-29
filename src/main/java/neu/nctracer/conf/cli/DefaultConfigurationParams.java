package neu.nctracer.conf.cli;

import java.util.HashMap;
import java.util.Map;

import neu.nctracer.exception.ParsingException;

/**
 * Default implementation to dynamic set/get configuration params required by
 * image-stitching algorithms
 * 
 * @author Ankur Shanbhag
 *
 */
public class DefaultConfigurationParams implements ConfigurationParams {

    private Map<String, String> configMap = new HashMap<>();

    /**
     * Overwrites all the parameters previously set in this object
     */
    @Override
    public void parseParams(String paramStr, String delimiter) throws ParsingException {
        String[] params = paramStr.split(delimiter);
        parseParams(params);
    }

    /**
     * Parse given key=value pairs to form parameters
     * 
     * @param params
     * @throws ParsingException
     */
    @Override
    public void parseParams(String[] params) throws ParsingException {
        for (String param : params) {
            String[] strings = param.split("\\s*=\\s*");
            if (strings.length != 2)
                throw new ParsingException("Malformed param [" + param + "]");

            String key = strings[0].trim().toLowerCase();
            String value = strings[1].trim();
            this.configMap.put(key, value);
        }
    }

    /**
     * Overwrites all the parameters previously set in the object
     */
    @Override
    public void setParams(Map<String, String> params) {
        // create a defensive copy to avoid referencing issues
        this.configMap = new HashMap<>(params);
    }

    @Override
    public void setParam(String name, String value) {
        this.configMap.put(name, value);
    }

    @Override
    public String getParam(String param) {
        return configMap.get(param);
    }

    @Override
    public String toString() {
        return configMap.toString();
    }

    @Override
    public String getParam(String param, String defaultValue) {
        String value = configMap.get(param);

        // return default, if not found
        if (null == value || value.isEmpty())
            return defaultValue;

        return value;
    }

    @Override
    public Map<String, String> getParams() {
        return new HashMap<>(this.configMap);
    }
}