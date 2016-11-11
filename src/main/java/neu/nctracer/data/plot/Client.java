package neu.nctracer.data.plot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import neu.nctracer.data.DataCorrespondence;
import neu.nctracer.exception.DataPlotException;
import neu.nctracer.exception.ParsingException;
import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;

/**
 * Main class to used for constructing scatter plots for debugging
 * 
 * @author Ankur Shanbhag
 *
 */
public class Client {

    public static void main(String[] args) {

        if (null == args || args.length < 1) {
            System.out.println("USAGE: "
                               + Client.class.getName()
                               + " <outputFilePath> [true|false] [true|false]");
            System.exit(0);
        }

        String filePath = args[0];
        boolean plot3d = true;
        boolean plotSideBySide = true;

        if (args.length >= 2)
            plot3d = Boolean.valueOf(args[1]);

        if (args.length >= 3)
            plotSideBySide = Boolean.valueOf(args[2]);

        Logger logger = LogManager.createLogger("default");
        LogManager.getLogManager().setDefaultLogger(logger);

        try {
            logger.info("Generating 3-D plot for correspondences");
            List<DataCorrespondence> correspondences = createDataCorrespondence(filePath);

            DataPlotter plotter = DataPlotManager.getDefaultPlotter();
            plotter.scatterPlot(correspondences, plot3d, plotSideBySide);

            logger.info("Successfully generated scatter plots");
        } catch (ParsingException e) {
            logger.fatal("Error parsing input data from file - " + filePath, e);
            System.exit(-1);
        } catch (IOException e) {
            logger.fatal("Error reading data from file - " + filePath, e);
            System.exit(-1);
        } catch (DataPlotException e) {
            logger.fatal("Error plotting points.", e);
            System.exit(-1);
        }
    }

    private static List<DataCorrespondence>
            createDataCorrespondence(String path) throws ParsingException, IOException {

        File file = new File(path);
        if (!file.isFile())
            throw new FileNotFoundException("Input file [" + file + "] does not exist.");

        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<DataCorrespondence> list = new ArrayList<>();

        String data = null;
        try {
            while ((data = reader.readLine()) != null) {
                list.add(DataCorrespondence.parse(data));
            }
        } finally {
            if (null != reader)
                reader.close();
        }

        return list;
    }
}

