package neu.nctracer.data.plot;

import java.util.List;

import neu.nctracer.data.DataObject;
import neu.nctracer.exception.DataPlotException;

/**
 * Interface for plotting data points
 * 
 * @author Ankur Shanbhag
 *
 */
public interface DataPlotter {

    void scatterPlot(List<List<DataObject>> data, boolean printFile) throws DataPlotException;
}
