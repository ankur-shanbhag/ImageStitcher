package neu.nctracer.client;

import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import neu.nctracer.conf.ConfigurationConstants;
import neu.nctracer.conf.ConfigurationManager;
import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.exception.InvalidConfigKeyException;
import neu.nctracer.log.GenericLogger;
import neu.nctracer.log.LogManager;
import neu.nctracer.mr.ImageStitcher;
import neu.nctracer.mr.ImageStitchingDriver;

public class Client {
    public static void main(String[] args)
            throws InvalidConfigKeyException,
            HdfsException,
            ConfigurationException,
            URISyntaxException {

        if (null == args || args.length != 2) {
            System.out.println("Invalid input!");
            System.out.println("Usage: Client <InputPath> <OutputPath>");
            System.exit(0);
        }

        ConfigurationManager handler = ConfigurationManager.getConfigurationManager();
        String hadoopConfDir = handler.getConfig(ConfigurationConstants.HADOOP_CONF_DIR);

        System.out.println("Hadoop conf dir ::: " + hadoopConfDir);
        Configuration conf = new Configuration();

        LogManager manager = LogManager.getLogManager();
        GenericLogger logger = manager.getDefaultLogger();

        logger.info("First log message from client ...");
        conf.addResource(new Path(hadoopConfDir + "/mapred-site.xml"));
        conf.addResource(new Path(hadoopConfDir + "/hdfs-site.xml"));
        conf.addResource(new Path(hadoopConfDir + "/core-site.xml"));

        ImageStitcher driver = new ImageStitchingDriver();
        driver.setup(conf, args[0], args[1]);
        driver.run();
    }
}
