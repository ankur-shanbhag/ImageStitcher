package neu.nctracer.data.plot;

/**
 * Used for injecting the default data plotter
 * 
 * @author Ankur Shanbhag
 *
 */
public class DataPlotManager {

    private DataPlotter plotter = null;

    private DataPlotManager() {
        this.plotter = new GnuPlotter();
    }

    public DataPlotter getDefaultPlotter() {
        return this.plotter;
    }
}
