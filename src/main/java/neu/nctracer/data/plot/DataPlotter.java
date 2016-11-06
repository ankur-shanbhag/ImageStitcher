package neu.nctracer.data.plot;

import java.util.List;

import neu.nctracer.data.DataCorrespondence;
import neu.nctracer.exception.DataPlotException;

/**
 * Interface for plotting data points
 * 
 * @author Ankur Shanbhag
 *
 */
public interface DataPlotter {

    void scatterPlot(List<DataCorrespondence> data,
                     boolean plot3D,
                     boolean plotSideBySide) throws DataPlotException;
}

