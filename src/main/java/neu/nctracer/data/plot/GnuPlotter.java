package neu.nctracer.data.plot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import neu.nctracer.conf.ConfigurationConstants;
import neu.nctracer.conf.ConfigurationManager;
import neu.nctracer.data.DataObject;
import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.DataPlotException;
import neu.nctracer.exception.InvalidConfigKeyException;
import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;

/**
 * Invokes underlying Gnuplot process to plot data points for visualization.
 * 
 * @author Ankur Shanbhag
 *
 */
public class GnuPlotter implements DataPlotter {

    private Logger logger = LogManager.getLogManager().getDefaultLogger();

    @Override
    public void scatterPlot(List<List<DataObject>> data,
                            boolean printFile) throws DataPlotException {

        String fileName = writeData(data);
        BufferedReader br = null;
        try {
            ProcessBuilder builder = buildGnuplotProcess(fileName);
            Process process = builder.start();
            InputStreamReader isr = new InputStreamReader(process.getErrorStream());
            br = new BufferedReader(isr);
            String line = null;

            while ((line = br.readLine()) != null) {
                logger.warn(line);
            }

            if (process.waitFor() != 0) {
                logger.warn("Gnuplot process terminated with non-zero status");
                throw new DataPlotException("Gnuplot process terminated with non-zero status");
            }
        } catch (IOException e) {
            throw new DataPlotException(e);
        } catch (InterruptedException e) {
            throw new DataPlotException(e);
        } catch (ConfigurationException e) {
            throw new DataPlotException(e);
        } finally {
            deleteFile(fileName);
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private ProcessBuilder buildGnuplotProcess(String fileName) throws ConfigurationException {

        ConfigurationManager manager = ConfigurationManager.getConfigurationManager();
        List<String> commands = new ArrayList<>();
        commands.add("/usr/bin/gnuplot");
        commands.add("-e");

        // add arguments for gnuplot script
        StringBuilder commandParms = new StringBuilder();
        commandParms.append("datafile='").append(fileName).append("';");
        commandParms.append("outputfile='");
        try {
            String outputPath = manager.getConfig(ConfigurationConstants.GNUPLOT_OUTPUT_PATH);
            commandParms.append(outputPath);
        } catch (InvalidConfigKeyException e) {
            commandParms.append("gnuplot-output.png");
        }
        commandParms.append("'");
        commands.add(commandParms.toString());

        // add gnuplot script
        try {
            String projectBaseDir = manager.getConfig(ConfigurationConstants.PROJECT_BASE_DIR);
            commands.add(projectBaseDir + "/conf/plotdata.gnu");
        } catch (InvalidConfigKeyException e) {
            // Ignore. should not occur
        }

        ProcessBuilder builder = new ProcessBuilder(commands);
        return builder;
    }

    private String writeData(List<List<DataObject>> data) throws DataPlotException {
        final String fileName = "gnuplot-" + System.currentTimeMillis() + ".tmp";

        PrintWriter writer = null;
        Random r = new Random();
        try {
            writer = new PrintWriter(fileName);
            for (List<DataObject> cluster : data) {
                int red = r.nextInt(256);
                int green = r.nextInt(256);
                int blue = r.nextInt(256);
                for (DataObject point : cluster) {
                    StringBuilder builder = new StringBuilder();
                    double[] features = point.getFeatures();
                    for (double feature : features) {
                        builder.append(feature).append(" ");
                    }
                    builder.append(red).append(" ").append(green).append(" ").append(blue);
                    writer.println(builder);
                }
            }

        } catch (FileNotFoundException e) {
            throw new DataPlotException();
        } finally {
            if (null != writer) {
                writer.flush();
                writer.close();
            }
        }
        return fileName;
    }

    private boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.isFile() ? file.delete() : false;
    }
}
