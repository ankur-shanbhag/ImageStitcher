package neu.nctracer.client;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import neu.nctracer.conf.ConfigurationManager;
import neu.nctracer.dm.conf.ConfigurationParams;
import neu.nctracer.dm.conf.DMConfigurationHandler;
import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.ParsingException;
import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;
import neu.nctracer.mr.ImageDataClusteringDriver;
import neu.nctracer.mr.ImageStitcher;

/**
 * Starting point for image stitching program
 * 
 * @author Ankur Shanbhag
 *
 */
public class Client {
    public static void main(String[] args) {

        if (null == args) {
            System.out.println("Invalid input!");
            System.exit(0);
        }

        Logger logger = LogManager.loggerInstance("default");
        LogManager.getLogManager().setDefaultLogger(logger);

        try {
            ConfigurationParams params = DMConfigurationHandler.getHandler()
                                                               .getConfigurationParamsInstance();
            params.parseParams(args);
            addConfigurationProperties(params);

            // Invoke one of many image stitching implementations. Can be made
            // configurable
            ImageStitcher driver = new ImageDataClusteringDriver();
            driver.setup(params);
            boolean status = driver.run();

            if (status) {
                logger.info("Image stitching job completed successfully.");
            } else {
                logger.error("Image stitching job terminated with errors. Check Hadoop log files for error details.");
            }
            System.exit(0);
        } catch (IOException e) {
            logger.fatal("Error performing HDFS operation..", e);
        } catch (ParsingException e) {
            logger.error("Error parsing arguments. " + e.getMessage(), e);
        } catch (ConfigurationException e) {
            logger.error("Error fetching configurations. " + e.getMessage(), e);
        }

        System.exit(-1);
    }

    /**
     * Adds all other parameters specified by the user in
     * <tt>configuration.properties</tt> file
     * 
     * @param params
     * @throws ParsingException
     * @throws ConfigurationException
     */
    private static void
            addConfigurationProperties(ConfigurationParams params) throws ParsingException,
                                                                   ConfigurationException {

        ConfigurationManager handler = ConfigurationManager.getConfigurationManager();
        Map<String, String> configurations = handler.getAllConfigurations();
        for (Entry<String, String> config : configurations.entrySet()) {
            params.setParam(config.getKey(), config.getValue());
        }
    }
}

