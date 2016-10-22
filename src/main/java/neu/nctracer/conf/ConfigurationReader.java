package neu.nctracer.conf;

import java.io.InputStream;

import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.InvalidConfigKeyException;

public interface ConfigurationReader {

    void loadConfigurations(InputStream inputStream) throws ConfigurationException;

    void reloadConfigurations() throws ConfigurationException;

    String getConfiguration(String key) throws InvalidConfigKeyException;
}