package neu.nctracer.conf.cli;

/**
 * Provides mechanism to inject an implementation of
 * {@linkplain ConfigurationParams} to all the requesting classes
 * 
 * @author Ankur Shanbhag
 */
public final class CLIConfigurationManager {
    private static CLIConfigurationManager handler = new CLIConfigurationManager();

    private CLIConfigurationManager() {
    }

    public static CLIConfigurationManager getHandler() {
        return handler;
    }

    public ConfigurationParams getConfigurationParamsInstance() {
        return new DefaultConfigurationParams();
    }
}