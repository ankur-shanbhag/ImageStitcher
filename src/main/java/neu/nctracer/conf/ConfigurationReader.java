package neu.nctracer.conf;

import java.io.InputStream;
import java.util.Map;

import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.InvalidConfigKeyException;

/**
 * Interface to be implemented by all the configuration reader implementations.
 * 
 * @author Ankur Shanbhag
 *
 */
public interface ConfigurationReader {

    void loadConfigurations(InputStream inputStream) throws ConfigurationException;

    void reloadConfigurations() throws ConfigurationException;

    String getConfiguration(String key) throws InvalidConfigKeyException;

    Map<String, String> getAllConfigurations();
}

