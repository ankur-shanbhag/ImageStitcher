package neu.nctracer.mr;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

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
    protected String localOutputPath;
    protected String hdfsSourceImagePath;
    protected String hdfsTargetImagePath;

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
    public void setup(ConfigurationParams params) throws HdfsException, IllegalArgumentException {
        this.conf = createConfiguration(params);

        this.hdfsBaseDirPath = params.getParam("hdfs.main.dir");
        createHdfsBaseDir(params.getParam("hdfs.dirs.delete"));

        this.hdfsOutputPath = hdfsBaseDirPath
                              + Path.SEPARATOR
                              + driverClass.getSimpleName()
                              + "_hdfs-output-dir";

        copyImageFilesToHdfs(params);

        this.localOutputPath = params.getParam("local.output.path", null);
        if (null == localOutputPath || localOutputPath.isEmpty())
            throw new IllegalArgumentException("Mandatory parameter [local.output.path] is not set. "
                                               + "This parameter specifies directory to copy stitching output on local machine.");
    }

    private void copyImageFilesToHdfs(ConfigurationParams params) throws HdfsException,
                                                                  IllegalArgumentException {
        logger.debug("Copying all image files to HDFS dir - " + hdfsBaseDirPath);
        String localSourceImage = params.getParam("local.image.source.file", null);
        this.hdfsSourceImagePath = HdfsFileUtils.copyFromLocal(localSourceImage,
                                                               hdfsBaseDirPath,
                                                               conf);
        if (null == localSourceImage)
            throw new IllegalArgumentException("Mandatory parameter [local.image.source.file] is not set. "
                                               + "This parameter specifies path to source image file.");

        String localTargetImage = params.getParam("local.image.target.file", null);
        if (null == localTargetImage)
            throw new IllegalArgumentException("Mandatory parameter [local.image.target.file] is not set. "
                                               + "This parameter specifies path to target image file.");

        this.hdfsTargetImagePath = HdfsFileUtils.copyFromLocal(localTargetImage,
                                                               hdfsBaseDirPath,
                                                               conf);
    }

    protected final Job createJobInstance() throws IOException {
        Job job = Job.getInstance(conf, driverClass.getSimpleName());
        job.setJarByClass(driverClass);
        return job;
    }

    private void createHdfsBaseDir(String deleteOutputDir) throws HdfsException {
        if (Boolean.valueOf(deleteOutputDir)) {
            logger.debug("Deleting HDFS base directory - " + hdfsBaseDirPath);
            HdfsFileUtils.delete(hdfsBaseDirPath, true, conf, true);
            logger.info("Successfully deleted HDFS base directory - " + hdfsBaseDirPath);

        } else if (HdfsFileUtils.isDir(hdfsBaseDirPath, conf, true)) {
            throw new HdfsException("HDFS base directory aready exists - "
                                    + hdfsBaseDirPath
                                    + ". Cannot start mapreduce job to perform image stitching.");
        }

        HdfsFileUtils.createDir(hdfsBaseDirPath, false, conf, true);
    }

    /**
     * Creates <code>Configuration</code> instance and adds necessary Hadoop
     * resources to it.
     * 
     * @param params
     */
    protected Configuration createConfiguration(ConfigurationParams params) {
        String hadoopConfDir = params.getParam("hadoop.conf.path");

        Configuration conf = new Configuration();
        conf.addResource(new Path(hadoopConfDir + "/mapred-site.xml"));
        conf.addResource(new Path(hadoopConfDir + "/hdfs-site.xml"));
        conf.addResource(new Path(hadoopConfDir + "/core-site.xml"));

        // Pass all the configurable parameters set by the user to mapreduce job
        Map<String, String> allParams = params.getParams();
        String[] strings = new String[allParams.size()];
        int i = 0;
        for (Entry<String, String> param : allParams.entrySet()) {
            strings[i++] = param.toString();
        }

        conf.setStrings("configurable.params", strings);
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
                            + libDir.getAbsolutePath()
                            + "]");
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

    protected void cleanup(Job job, boolean status) throws IOException, HdfsException {
        if (status) {
            if (job.isSuccessful()) {
                logger.info("Mapreduce job with id ["
                            + job.getJobID()
                            + "] completed successfully.");

                if (HdfsFileUtils.isDir(localOutputPath, conf, false)) {
                    HdfsFileUtils.delete(localOutputPath, true, conf, false);
                }
                HdfsFileUtils.copyToLocal(hdfsOutputPath, localOutputPath, conf);
                logger.info("HDFS output files copied to local file system at location - "
                            + new File(localOutputPath).getAbsolutePath());
            } else {
                logger.error("Mapreduce job completed with error. Check hadoop logs for details.");
            }
        } else {
            logger.error("Mapreduce job terminated with errors. Check hadoop logs for details.");
        }
    }
}

