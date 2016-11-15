package neu.nctracer.client;

import java.io.IOException;

import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;
import neu.nctracer.mr.ImageStitcher;
import neu.nctracer.mr.ImageStitchingDriver;

/**
 * Starting point for image stitching program
 * 
 * @author Ankur Shanbhag
 *
 */
public class Client {
    public static void main(String[] args) {

        if (null == args || args.length != 2) {
            System.out.println("Invalid input!");
            System.out.println("Usage: Client <LocalInputPath> <LocalOutputPath>");
            System.exit(0);
        }

        final String localInputPath = args[0];
        final String localOutputPath = args[1];

        Logger logger = LogManager.loggerInstance("default");
        LogManager.getLogManager().setDefaultLogger(logger);

        try {
            // Invoke one of many image stitching implementations. Can be made
            // configurable
            ImageStitcher driver = new ImageStitchingDriver();
            driver.setup(localInputPath, localOutputPath);
            boolean status = driver.run();

            if (status) {
                logger.info("Image stitching job completed successfully.");
            } else {
                logger.error("Image stitching job terminated with errors. Check Hadoop log files for error details.");
            }
            System.exit(0);
        } catch (IOException e) {
            logger.fatal("Error performing HDFS operation.", e);
        }

        System.exit(-1);
    }
}

