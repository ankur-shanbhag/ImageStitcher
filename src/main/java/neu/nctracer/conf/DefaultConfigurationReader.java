package neu.nctracer.conf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.InvalidConfigKeyException;

/**
 * Default implementation provided to read configuration parameters set by the
 * user. Expects the configurations to be of the form "key=value"
 * 
 * @author Ankur Shanbhag
 */
class DefaultConfigurationReader implements ConfigurationReader {

    private final Map<String, String> propertyMap = new HashMap<>();
    private InputStream inputStream = null;

    DefaultConfigurationReader() {
        // Limit instantiation of this class to classes within same package
    }

    /**
     * Reads and loads all the properties in memory from specified configuration
     * file. Call to this method is not thread safe.
     */
    @Override
    public void loadConfigurations(InputStream inputStream) throws ConfigurationException {

        this.inputStream = inputStream;

        try {
            Properties properties = new Properties();
            properties.load(inputStream);
            storePropertiesInMemory(properties);
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Configuration file does not exist.", e);
        } catch (IOException e) {
            throw new ConfigurationException("Error reading properties.", e);
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Reloads all the properties in memory. Call to this method is not thread
     * safe.
     */
    @Override
    public void reloadConfigurations() throws ConfigurationException {
        propertyMap.clear();
        loadConfigurations(inputStream);
    }

    @Override
    public String getConfiguration(String key) throws InvalidConfigKeyException {
        if (null == key)
            return null;
        if (propertyMap.containsKey(key))
            return propertyMap.get(key);

        throw new InvalidConfigKeyException("Invalid");
    }

    private void storePropertiesInMemory(Properties properties) {
        Enumeration<?> propertyNames = properties.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            String value = properties.getProperty(key);
            propertyMap.put(key, value);
        }
    }

}