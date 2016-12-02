package neu.nctracer.data.plot;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JFrame;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.swing.JPlot;

import neu.nctracer.conf.ConfigurationConstants;
import neu.nctracer.conf.ConfigurationManager;
import neu.nctracer.data.DataCorrespondence;
import neu.nctracer.data.DataObject;
import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.DataPlotException;
import neu.nctracer.exception.InvalidConfigKeyException;
import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;

/**
 * Java based implementation for plotting graphs using GnuPlot. The class makes
 * use of JavaPlot library which connects to the underlying GnuPlot for
 * rendering. For details please visit <a>http://javaplot.panayotis.com/</a><br>
 * <br>
 * 
 * <u>Note:</u> It is required to have GnuPlot installed on the system for
 * plotting graphs. For details about GnuPlot visit
 * <a>http://www.gnuplot.info/</a>
 * 
 * @author Ankur Shanbhag
 *
 */
public class JavaPlotter implements DataPlotter {

    // Use this path if not specified by the user
    private static final String GNUPLOT_EXEC_DEFAULT_LOCATION = "/usr/bin/gnuplot";

    /* Axes labels to be displayed on the plot */
    private static final String X_AXIS_LABEL = "x-axis";
    private static final String Y_AXIS_LABEL = "y-axis";
    private static final String Z_AXIS_LABEL = "z-axis";

    private Logger logger = LogManager.getLogManager().getDefaultLogger();

    @Override
    public void scatterPlot(List<DataCorrespondence> correspondences,
                            boolean plot3d,
                            boolean superImpose) throws DataPlotException {

        String gnuplotExec = getGnuPlotExecutablePath();

        if (superImpose) {
            JPlot superImposed = createPlot(gnuplotExec, plot3d, "Superimposed View");

            for (int i = 0; i < correspondences.size(); i++) {
                DataCorrespondence correspondence = correspondences.get(i);
                plotPoints(superImposed,
                           i + 1,
                           correspondence.getTranslatedSource(),
                           correspondence.getTarget());
            }
            renderPlots(superImposed);
        } else {
            JPlot sourcePlot = createPlot(gnuplotExec, plot3d, "Source Points");
            JPlot targetPlot = createPlot(gnuplotExec, plot3d, "Target Points");

            for (int i = 0; i < correspondences.size(); i++) {
                DataCorrespondence correspondence = correspondences.get(i);
                plotPoints(sourcePlot, i + 1, correspondence.getSource());
                plotPoints(targetPlot, i + 1, correspondence.getTarget());
            }
            renderPlots(sourcePlot, targetPlot);
        }
    }

    /**
     * Places all the given JPlots on a single window for rendering. All the
     * plots are arranged horizontally on the window frame. The plots will be
     * readjusted and resized when user manually resizes the window
     * 
     * @param plots
     */
    private void renderPlots(final JPlot... plots) {
        JFrame frame = new JFrame();
        frame.setLayout(new GridLayout(plots.length, 1));
        for (JPlot plot : plots)
            frame.add(plot);

        // This listener is needed to resize the plot when user resizes the
        // window
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                Component frame = (Component) evt.getSource();

                int newHeight = frame.getHeight() / plots.length;
                int newWidth = frame.getWidth();

                for (JPlot plot : plots) {
                    plot.getJavaPlot().set("term png size", newWidth + "," + newHeight);
                    plot.plot();
                    plot.repaint();
                }
            }
        });

        setDefaultFrameSize(frame);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Sets default size of the frame to half of the screen resolution
     * 
     * @param frame
     */
    private void setDefaultFrameSize(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) screenSize.getWidth() / 2, (int) screenSize.getHeight() / 2);
    }

    private void plotPoints(JPlot plot, int type, DataObject... objects) {
        double[][] data = new double[objects.length][];
        for (int i = 0; i < data.length; i++) {
            data[i] = objects[i].getFeatures();
        }

        DataSetPlot dataPlot = new DataSetPlot(data);
        dataPlot.setTitle("");
        dataPlot.setPlotStyle(getPlotStyle(type));

        JavaPlot p = plot.getJavaPlot();
        p.addPlot(dataPlot);
    }

    private PlotStyle getPlotStyle(int type) {
        PlotStyle myStyle = new PlotStyle();
        myStyle.setStyle(Style.POINTS);
        myStyle.setPointSize(1);
        myStyle.setPointType(type);
        return myStyle;
    }

    private JPlot createPlot(String gnuplotExec, boolean plot3d, String title) {

        JavaPlot javaPlot = new JavaPlot(gnuplotExec, plot3d);
        final JPlot plot = new JPlot(javaPlot);
        final JavaPlot p = plot.getJavaPlot();
        p.setTitle(title);

        addMouseListernerForRotation(plot);
        setPlotProperties(p, plot3d);
        return plot;
    }

    private String getGnuPlotExecutablePath() throws DataPlotException {
        String gnuplotExec = null;
        try {
            gnuplotExec = ConfigurationManager.getConfigurationManager()
                                              .getConfig(ConfigurationConstants.GNUPLOT_PATH);
            logger.info("GnuPlot executable path set to - " + gnuplotExec);
        } catch (InvalidConfigKeyException e) {
            // set to default
            gnuplotExec = GNUPLOT_EXEC_DEFAULT_LOCATION;
            logger.info("GnuPlot executable path not set by the user. Using default path - "
                        + gnuplotExec);
        } catch (ConfigurationException e) {
            logger.error("Error initializing configuration manager.", e);
            throw new DataPlotException(e);
        }
        return gnuplotExec;
    }

    private void setPlotProperties(final JavaPlot p, boolean plot3d) {
        if (plot3d) {
            p.newGraph3D();
            p.getAxis("z").setLabel(Z_AXIS_LABEL);
        } else {
            // 2D graph
            p.newGraph();
        }

        p.getAxis("x").setLabel(X_AXIS_LABEL);
        p.getAxis("y").setLabel(Y_AXIS_LABEL);

        p.set("lmargin at screen", "0.25");
        p.set("rmargin at screen", "0.75");
        if (!plot3d) {
            p.set("tmargin at screen", "0.25");
            p.set("bmargin at screen", "0.75");
        }
    }

    private void addMouseListernerForRotation(final JPlot plot) {
        plot.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // No action
            }

            /**
             * Rotate the plot and repaint when user drags it
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

                double rotX = (double) x / plot.getWidth() * 360;
                double rotY = (double) y / plot.getHeight() * 360;

                // range check
                if (rotX < 0) {
                    rotX = 0;
                }
                if (rotX > 360) {
                    rotX = 360;
                }
                if (rotY < 0) {
                    rotY = 0;
                }
                if (rotY > 360) {
                    rotY = 360;
                }

                // set view
                plot.getJavaPlot().set("view", rotY + "," + rotX);

                // repaint
                plot.plot();
                plot.repaint();
            }
        });
    }
}

