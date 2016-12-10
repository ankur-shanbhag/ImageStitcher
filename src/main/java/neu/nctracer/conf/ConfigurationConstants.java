package neu.nctracer.conf;

/**
 * Constants to be read from <tt>configuration.properties</tt> file
 * 
 * @author Ankur Shanbhag
 */
public final class ConfigurationConstants {

    public static final String PROJECT_BASE_DIR = "project.base.path";

    public static final String LOCAL_SOURCE_IMAGE_FILE = "local.image.source.file";
    public static final String LOCAL_TARGET_IMAGE_FILE = "local.image.target.file";

    /* Hadoop configuration parameters */
    public static final String HADOOP_HOME = "hadoop.home";
    public static final String HADOOP_CONF_DIR = "hadoop.conf.path";

    // HDFS configurations
    public static final String HDFS_BASE_DIR = "hdfs.main.dir";
    public static final String DELETE_HDFS_DIRS = "hdfs.dirs.delete";

    public static final String GNUPLOT_PATH = "gnuplot.process.path";
    
    private ConfigurationConstants() {
        // Deny object creation
    }
}

