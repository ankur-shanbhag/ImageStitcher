package neu.nctracer.client;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import neu.nctracer.conf.ConfigurationManager;
import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.conf.cli.CLIConfigurationManager;
import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.ParsingException;
import neu.nctracer.exception.ReflectionUtilsException;
import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;
import neu.nctracer.mr.ImageStitcher;
import neu.nctracer.mr.PointToPointTranslationDriver;
import neu.nctracer.utils.ReflectionUtils;

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
            ConfigurationParams params = CLIConfigurationManager.getHandler()
                                                                .getConfigurationParamsInstance();
            params.parseParams(args);
            addConfigurationProperties(params);

            ImageStitcher driver = instantiateDriverClass(params, logger);

            logger.info("Setting Image Stitching driver class as ["
                        + driver.getClass().getName()
                        + "]");

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
     * Invoke one of many image stitching implementations. Can be made set
     * externally by the user
     */
    private static ImageStitcher instantiateDriverClass(ConfigurationParams params, Logger logger) {
        String driverClassName = params.getParam("image.stitching.driver.class");
        final ImageStitcher defaultClassInstance = new PointToPointTranslationDriver();

        if (null == driverClassName) {
            // Not specified by user. Setting to default
            return defaultClassInstance;
        }

        try {
            return ReflectionUtils.instantiate(driverClassName, ImageStitcher.class);
        } catch (ReflectionUtilsException e) {
            logger.warn("Specified class ["
                        + driverClassName
                        + "] cannot be instantiated. "
                        + "Invalid argument specified for parameter [image.stitching.driver.class].");
            return defaultClassInstance;
        }
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
