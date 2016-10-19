package neu.nctracer.client;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import neu.nctracer.conf.ConfigurationConstants;
import neu.nctracer.conf.ConfigurationManager;
import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.InvalidConfigKeyException;
import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;
import neu.nctracer.mr.ImageStitcher;
import neu.nctracer.mr.ImageStitchingDriver;

public class Client {
    public static void main(String[] args) {

        if (null == args || args.length != 2) {
            System.out.println("Invalid input!");
            System.out.println("Usage: Client <LocalInputPath> <LocalOutputPath>");
            System.exit(0);
        }

        final String localInputPath = args[0];
        final String localOutputPath = args[1];

        Logger logger = LogManager.getLogManager().getDefaultLogger();

        try {
            ConfigurationManager handler = ConfigurationManager.getConfigurationManager();
            String hadoopConfDir = handler.getConfig(ConfigurationConstants.HADOOP_CONF_DIR);

            Configuration conf = new Configuration();
            conf.addResource(new Path(hadoopConfDir + "/mapred-site.xml"));
            conf.addResource(new Path(hadoopConfDir + "/hdfs-site.xml"));
            conf.addResource(new Path(hadoopConfDir + "/core-site.xml"));

            ImageStitcher driver = new ImageStitchingDriver();
            driver.setup(conf, localInputPath, localOutputPath);
            boolean status = driver.run();

            if (status) {
                logger.info("Image stitching job completed successfully.");
            } else {
                logger.error("Image stitching job terminated with errors. Check Hadoop log files for error details.");
            }
            System.exit(0);
        } catch (ConfigurationException e) {
            logger.fatal("Error reading configurations.", e);
        } catch (InvalidConfigKeyException e) {
            logger.fatal("Configurations property not found.", e);
        } catch (IOException e) {
            logger.fatal("Error performing HDFS operation.", e);
        }

        System.exit(-1);
    }
}
