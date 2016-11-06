package neu.nctracer.data.plot;

/**
 * Used for injecting the default data plotter
 * 
 * @author Ankur Shanbhag
 *
 */
public class DataPlotManager {

    private static DataPlotter defaultPlotter = new JavaPlotter();

    private DataPlotManager() {
    }

    public static DataPlotter getDefaultPlotter() {
        return defaultPlotter;
    }
}

