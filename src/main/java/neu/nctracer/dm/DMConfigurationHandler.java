package neu.nctracer.dm;

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
