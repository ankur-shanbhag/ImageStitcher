package neu.nctracer.mr;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import neu.nctracer.conf.ConfigurationConstants;
import neu.nctracer.conf.ConfigurationManager;
import neu.nctracer.dm.conf.ConfigurationParams;
import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.exception.InvalidConfigKeyException;
import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;
import neu.nctracer.utils.HdfsFileUtils;

/**
 * Base class to perform common activities for all map-reduce driver classes
 * 
 * @author Ankur Shanbhag
 *
 */
public abstract class MapReduceStitchingDriver implements ImageStitcher {

    protected Logger logger = LogManager.getLogManager().getDefaultLogger();

    protected Configuration conf;
    protected String hdfsBaseDirPath;
    protected String hdfsOutputPath;

    private final Class<? extends MapReduceStitchingDriver> driverClass;

    public MapReduceStitchingDriver(Class<? extends MapReduceStitchingDriver> driverClass) {
        this.driverClass = driverClass;
    }

    /**
     * Sets up HDFS base directory required to perform any mapreduce operation.
     * This directory will hold all the files i.e. input, output directory etc.
     * required by the stitching jobs
     */
    @Override
    public void setup(ConfigurationParams params) throws HdfsException {
        this.conf = createConfiguration(params.getParam("hadoop.conf.path"));

        this.hdfsBaseDirPath = params.getParam("hdfs.main.dir");
        createHdfsBaseDir(params.getParam("hdfs.dirs.delete"));

        this.hdfsOutputPath = hdfsBaseDirPath
                              + Path.SEPARATOR
                              + driverClass.getSimpleName()
                              + "_hdfs-output-dir";
    }

    protected Job createJobInstance() throws IOException {
        Job job = Job.getInstance(conf, driverClass.getSimpleName());
        job.setJarByClass(driverClass);
        return job;
    }

    private void createHdfsBaseDir(String deleteOutputDir) throws HdfsException {
        if (Boolean.valueOf(deleteOutputDir)) {
            logger.debug("Deleting HDFS base directory - " + hdfsBaseDirPath);
            HdfsFileUtils.delete(hdfsBaseDirPath, true, conf);
            logger.info("Successfully deleted HDFS base directory - " + hdfsBaseDirPath);

        } else if (HdfsFileUtils.isDir(hdfsBaseDirPath, conf)) {
            throw new HdfsException("HDFS base directory aready exists - "
                                    + hdfsBaseDirPath
                                    + ". Cannot start mapreduce job to perform image stitching.");
        }

        HdfsFileUtils.createDir(hdfsBaseDirPath, false, conf);
    }

    /**
     * Creates <code>Configuration</code> instance and adds necessary Hadoop
     * resources to it.
     * 
     * @param hadoopConfDir
     */
    protected Configuration createConfiguration(String hadoopConfDir) {
        Configuration conf = new Configuration();
        conf.addResource(new Path(hadoopConfDir + "/mapred-site.xml"));
        conf.addResource(new Path(hadoopConfDir + "/hdfs-site.xml"));
        conf.addResource(new Path(hadoopConfDir + "/core-site.xml"));
        return conf;
    }

    /**
     * Add all the dependent jar files to distributed cache
     * 
     * @param job
     * @throws HdfsException
     */
    protected void addJarsToDistributedCache(Job job) throws IOException {
        logger.debug("Adding all the dependent jar files to distributed cache.");
        try {
            ConfigurationManager handler = ConfigurationManager.getConfigurationManager();
            String projectHome = handler.getConfig(ConfigurationConstants.PROJECT_BASE_DIR);

            Configuration conf = job.getConfiguration();
            FileSystem fs = FileSystem.getLocal(conf);

            File libDir = new File(projectHome, "/target/lib");
            String localJars = cacheLocalJars(libDir, fs);
            if (null == localJars || localJars.isEmpty()) {
                logger.warn("No local dependent jar files found at location ["
                            + projectHome
                            + "/target/lib ]");
                return;
            }

            final String hadoopTmpJars = "tmpjars";
            String tmpJars = (null == conf.get(hadoopTmpJars)) ? localJars
                                                               : (conf.get(hadoopTmpJars)
                                                                  + ","
                                                                  + localJars);

            conf.set(hadoopTmpJars, tmpJars);
            logger.debug("Dependent jar files added successfully. [" + localJars + "]");
        } catch (ConfigurationException e) {
            throw new HdfsException(e.getMessage(), e);
        } catch (InvalidConfigKeyException e) {
            throw new HdfsException(e.getMessage(), e);
        }
    }

    protected String cacheLocalJars(File libDir, FileSystem fs) {
        StringBuilder builder = new StringBuilder();

        if (!libDir.isDirectory())
            return null;

        for (File file : libDir.listFiles()) {
            if (!file.getName().endsWith(".jar"))
                continue;

            Path path = new Path(file.toString());
            @SuppressWarnings("deprecation")
            String qualified = path.makeQualified(fs).toString();
            builder.append(qualified).append(",");
        }

        builder.delete(builder.length() - 1, builder.length());
        return builder.toString();
    }
}
