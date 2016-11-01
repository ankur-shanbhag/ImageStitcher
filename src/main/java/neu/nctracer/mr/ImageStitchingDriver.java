package neu.nctracer.mr;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import neu.nctracer.conf.ConfigurationConstants;
import neu.nctracer.conf.ConfigurationManager;
import neu.nctracer.exception.ConfigurationException;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.exception.InvalidConfigKeyException;
import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;
import neu.nctracer.utils.HdfsFileUtils;

/**
 * Sets up resources needed by mapreduce job to perform image stitching
 * operation.
 * 
 * @author Ankur Shanbhag
 */
public class ImageStitchingDriver implements ImageStitcher {

    private Configuration conf;

    private String inputPath;
    private String outputPath;

    private String hdfsInputPath = null;
    private String hdfsOutputPath = null;

    private String hdfsSourceImage = null;
    private String hdfsTargetImage = null;
    private String threshold = null;

    private Logger logger = LogManager.getLogManager().getDefaultLogger();

    /**
     * Copies all the required files to HDFS required by the mapreduce job
     */
    public void setup(String localInputPath, String localOutputPath) throws HdfsException {
        this.inputPath = localInputPath;
        this.outputPath = localOutputPath;

        try {
            ConfigurationManager handler = ConfigurationManager.getConfigurationManager();

            this.conf = createConfiguration(handler);
            hdfsSourceImage = handler.getConfig(ConfigurationConstants.HDFS_SOURCE_IMAGE_PATH);
            hdfsTargetImage = handler.getConfig(ConfigurationConstants.HDFS_TARGET_IMAGE_FILE);
            threshold = handler.getConfig(ConfigurationConstants.ERROR_THRESHOLD);

            hdfsInputPath = handler.getConfig(ConfigurationConstants.HDFS_INPUT_DIR);
            hdfsOutputPath = handler.getConfig(ConfigurationConstants.HDFS_OUTPUT_DIR);

            String localSourceImage = handler.getConfig(ConfigurationConstants.LOCAL_SOURCE_IMAGE_FILE);
            String localTargetImage = handler.getConfig(ConfigurationConstants.LOCAL_TARGET_IMAGE_FILE);

            String deleteOutputDir = handler.getConfig(ConfigurationConstants.DELETE_HDFS_DIRS);
            if (Boolean.valueOf(deleteOutputDir)) {
                logger.debug("Deleting HDFS files and directories.");
                HdfsFileUtils.delete(hdfsInputPath, true, conf);
                HdfsFileUtils.delete(hdfsOutputPath, true, conf);
                HdfsFileUtils.delete(hdfsSourceImage, false, conf);
                HdfsFileUtils.delete(hdfsTargetImage, false, conf);
                logger.info("Successfully deleted all HDFS files and directories.");
            }

            logger.debug("Deleting HDFS files and directories.");
            HdfsFileUtils.copyFromLocal(inputPath, hdfsInputPath, conf);
            HdfsFileUtils.copyFromLocal(localTargetImage, hdfsTargetImage, conf);
            HdfsFileUtils.copyFromLocal(localSourceImage, hdfsSourceImage, conf);

            logger.info("Image stitching job setup successful.");
        } catch (InvalidConfigKeyException e) {
            logger.error("Cannot find Hadoop job parameters.", e);
            throw new HdfsException("Cannot find Hadoop job parameters.", e);
        } catch (ConfigurationException e) {
            logger.error("Cannot find Hadoop job parameters.", e);
            throw new HdfsException("Cannot find Hadoop job parameters.", e);
        } catch (HdfsException e) {
            logger.error("Error performing HDFS file operation.", e);
            throw e;
        }
    }

    private Configuration
            createConfiguration(ConfigurationManager handler) throws InvalidConfigKeyException {

        String hadoopConfDir = handler.getConfig(ConfigurationConstants.HADOOP_CONF_DIR);
        Configuration conf = new Configuration();
        conf.addResource(new Path(hadoopConfDir + "/mapred-site.xml"));
        conf.addResource(new Path(hadoopConfDir + "/hdfs-site.xml"));
        conf.addResource(new Path(hadoopConfDir + "/core-site.xml"));
        return conf;
    }

    /**
     * Copies all required libraries to mapreduce job class path. Kicks off the
     * mapreduce job. Every input line specifies configurations defined for
     * clustering data points.
     */
    public boolean run() throws HdfsException {

        try {
            Job job = Job.getInstance(conf, ImageStitchingDriver.class.getSimpleName());
            job.setJarByClass(ImageStitchingDriver.class);
            job.setMapperClass(ImageDataClusteringMapper.class);

            job.getConfiguration().setInt("mapreduce.input.lineinputformat.linespermap", 3);
            job.getConfiguration().set(HdfsConstants.SOURCE_IMAGE_HDFS_PATH, hdfsSourceImage);
            job.getConfiguration().set(HdfsConstants.TARGET_IMAGE_HDFS_PATH, hdfsTargetImage);
            job.getConfiguration().set(HdfsConstants.IMAGE_MATCHING_ERROR, threshold);

            // add all required jars to mapreduce job
            addJarToDistributedCache(job);

            NLineInputFormat.addInputPath(job, new Path(hdfsInputPath));
            job.setInputFormatClass(NLineInputFormat.class);

            // map only job
            job.setNumReduceTasks(0);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(NullWritable.class);

            FileOutputFormat.setOutputPath(job, new Path(hdfsOutputPath));

            logger.info("Starting mapreduce job to perform image stitching operation.");
            boolean status = job.waitForCompletion(true);
            if (status) {
                logger.info("Mapreduce job completed successfully.");
                HdfsFileUtils.copyToLocal(hdfsOutputPath, outputPath, conf);
                logger.debug("HDFS output files copied to local file system");
            } else {
                logger.error("Mapreduce job terminated with errors. Check hadoop logs for details.");
            }
            return status;
        } catch (IOException e) {
            throw new HdfsException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new HdfsException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new HdfsException(e.getMessage(), e);
        }
    }

    /**
     * Add all the dependent jar files to distributed cache
     * 
     * @param job
     * @throws HdfsException
     */
    private void addJarToDistributedCache(Job job) throws IOException {
        logger.debug("Adding all the dependent jar files to distributed cache.");
        try {
            ConfigurationManager handler = ConfigurationManager.getConfigurationManager();
            String projectHome = handler.getConfig(ConfigurationConstants.PROJECT_BASE_DIR);

            Configuration conf = job.getConfiguration();
            FileSystem fs = FileSystem.getLocal(conf);

            String localJars = cacheLocalJars(projectHome, fs);
            if (null == localJars || localJars.isEmpty()) {
                logger.warn("No local dependent jar files found.");
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

    private String cacheLocalJars(String projectHome, FileSystem fs) {
        StringBuilder builder = new StringBuilder();
        File libDir = new File(projectHome, "/lib");
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
