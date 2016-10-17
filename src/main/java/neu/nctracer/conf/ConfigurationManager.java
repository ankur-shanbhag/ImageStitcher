package neu.nctracer.conf;

import java.io.InputStream;

import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.InvalidConfigKeyException;

/**
 * Default configuration manager to be used across all classes.
 * 
 * @author Ankur Shanbhag
 */
public class ConfigurationManager {

    private static ConfigurationManager configManager = null;

    private ConfigurationReader defaultConfigReader;
    private final String configFile = "configuration.properties";

    private ConfigurationManager() throws ConfigurationException {
        InputStream stream = ConfigurationManager.class.getClassLoader()
                .getResourceAsStream(configFile);
        defaultConfigReader = new DefaultConfigurationReader();
        defaultConfigReader.loadConfigurations(stream);
    }

    public static ConfigurationManager getConfigurationManager() throws ConfigurationException {
        if (null == configManager) {
            // double checked locking to ensure single object creation
            synchronized (ConfigurationManager.class) {
                if (null == configManager) {
                    configManager = new ConfigurationManager();
                }
            }
        }
        return configManager;
    }

    public String getConfig(String key) throws InvalidConfigKeyException {
        return defaultConfigReader.getConfiguration(key);
    }

    public void setDefaultReader(ConfigurationReader reader) {
        defaultConfigReader = reader;
    }

    public void setDefaultReaderAndLoad(ConfigurationReader reader) throws ConfigurationException {
        defaultConfigReader = reader;
        InputStream stream = ConfigurationManager.class.getClassLoader()
                .getResourceAsStream(configFile);
        defaultConfigReader.loadConfigurations(stream);
    }
}
