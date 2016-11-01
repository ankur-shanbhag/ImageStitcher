package neu.nctracer.dm;

import java.util.HashMap;
import java.util.Map;

import neu.nctracer.exception.ParsingException;

/**
 * Default implementation to dynamic set/get configuration params required by
 * data mining algorithms
 * 
 * @author Ankur Shanbhag
 *
 */
public class DefaultConfigurationParams implements ConfigurationParams {

    private Map<String, String> configMap = new HashMap<>();

    @Override
    public void parseParams(String paramStr, String delimiter) throws ParsingException {
        this.configMap = new HashMap<>();
        String[] params = paramStr.split(delimiter);
        for (String param : params) {
            String[] strings = param.split("\\s*=\\s*");
            if (strings.length != 2)
                throw new ParsingException("Malformed param [" + param + "]");

            String key = strings[0].trim().toLowerCase();
            String value = strings[1].trim();
            this.configMap.put(key, value);
        }
    }

    @Override
    public void setParams(Map<String, String> params) {
        this.configMap = new HashMap<>(params);
    }

    @Override
    public String getParam(String param) {
        return configMap.get(param);
    }

    @Override
    public String toString() {
        return configMap.toString();
    }

}
