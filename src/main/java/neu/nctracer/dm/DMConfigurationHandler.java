package neu.nctracer.dm;

/**
 * Provides mechanism to inject some implementation of
 * {@linkplain ConfigurationParams} to all the requesting classes
 * 
 * @author Ankur Shanbhag
 */
public final class DMConfigurationHandler {
    private static DMConfigurationHandler handler = new DMConfigurationHandler();

    private DMConfigurationHandler() {
    }

    public static DMConfigurationHandler getHandler() {
        return handler;
    }

    public ConfigurationParams getConfigurationParamsInstance() {
        return new DefaultConfigurationParams();
    }
}
