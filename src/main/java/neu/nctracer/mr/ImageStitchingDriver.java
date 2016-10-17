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
import neu.nctracer.utils.HdfsFileUtils;

public class ImageStitchingDriver implements ImageStitcher {

    private Configuration conf;

    private String inputPath;
    private String outputPath;

    private String hdfsInputPath = null;
    private String hdfsOutputPath = null;

    private String hdfsSourceImage = null;
    private String hdfsTargetImage = null;
    private String threshold = null;

    public void setup(Configuration conf, String localInputPath, String localOutputPath)
            throws HdfsException {
        this.conf = conf;
        this.inputPath = localInputPath;
        this.outputPath = localOutputPath;

        try {
            ConfigurationManager handler = ConfigurationManager.getConfigurationManager();

            String deleteOutputDir = handler
                    .getConfig(ConfigurationConstants.DELETE_HDFS_DIRS);

            hdfsInputPath = handler.getConfig(ConfigurationConstants.HDFS_INPUT_DIR);
            hdfsOutputPath = handler.getConfig(ConfigurationConstants.HDFS_OUTPUT_DIR);

            if (Boolean.valueOf(deleteOutputDir)) {
                // TODO: Check if deletion fails
                HdfsFileUtils.delete(hdfsInputPath, true, conf);
                HdfsFileUtils.delete(hdfsOutputPath, true, conf);
            }

            HdfsFileUtils.copyFromLocal(inputPath, hdfsInputPath, conf);

            String localSourceImage = handler
                    .getConfig(ConfigurationConstants.LOCAL_SOURCE_IMAGE_FILE);
            hdfsSourceImage = handler.getConfig(ConfigurationConstants.HDFS_SOURCE_IMAGE_PATH);

            HdfsFileUtils.copyFromLocal(localSourceImage, hdfsSourceImage, conf);

            String localTargetImage = handler
                    .getConfig(ConfigurationConstants.LOCAL_TARGET_IMAGE_FILE);
            hdfsTargetImage = handler.getConfig(ConfigurationConstants.HDFS_TARGET_IMAGE_FILE);

            HdfsFileUtils.copyFromLocal(localTargetImage, hdfsTargetImage, conf);

            threshold = handler.getConfig(ConfigurationConstants.ERROR_THRESHOLD);

        } catch (InvalidConfigKeyException e) {
            throw new HdfsException("Cannot find Hadoop job parameters.", e);
        } catch (ConfigurationException e) {
            throw new HdfsException("Cannot find Hadoop job parameters.", e);
        }
    }

    public boolean run() throws HdfsException {

        try {
            Job job = Job.getInstance(conf, "Image Stiching Driver");
            job.setJarByClass(ImageStitchingDriver.class);
            job.setMapperClass(ImageDataClusteringMapper.class);

            job.setInputFormatClass(NLineInputFormat.class);

            NLineInputFormat.addInputPath(job, new Path(hdfsInputPath));
            job.getConfiguration().setInt("mapreduce.input.lineinputformat.linespermap", 1);

            job.getConfiguration().set(HdfsConstants.SOURCE_IMAGE_HDFS_PATH, hdfsSourceImage);
            job.getConfiguration().set(HdfsConstants.TARGET_IMAGE_HDFS_PATH, hdfsTargetImage);

            job.getConfiguration().set(HdfsConstants.IMAGE_MATCHING_ERROR, threshold);

            addJarToDistributedCache(job);

            job.setNumReduceTasks(0);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(NullWritable.class);

            FileOutputFormat.setOutputPath(job, new Path(hdfsOutputPath));

            return job.waitForCompletion(true);
        } catch (IOException e) {
            throw new HdfsException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new HdfsException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new HdfsException(e.getMessage(), e);
        }
    }

    private void addJarToDistributedCache(Job job)
            throws IOException, HdfsException {

        try {
            ConfigurationManager handler = ConfigurationManager.getConfigurationManager();
            String projectHome = handler.getConfig(ConfigurationConstants.PROJECT_BASE_DIR);

            Configuration conf = job.getConfiguration();
            FileSystem fs = FileSystem.getLocal(conf);

            String localJars = cacheLocalJars(projectHome, fs);
            if (null == localJars || localJars.isEmpty())
                return;

            final String hadoopTmpJars = "tmpjars";
            String tmpJars = (null == conf.get(hadoopTmpJars)) ? localJars
                    : conf.get(hadoopTmpJars) + "," + localJars;

            conf.set(hadoopTmpJars, tmpJars);
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
