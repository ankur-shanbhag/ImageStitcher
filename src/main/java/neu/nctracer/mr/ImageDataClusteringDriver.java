package neu.nctracer.mr;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import neu.nctracer.dm.conf.ConfigurationParams;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.utils.HdfsFileUtils;

/**
 * Sets up resources needed by mapreduce job to perform image stitching
 * operation.
 * 
 * @author Ankur Shanbhag
 */
public class ImageDataClusteringDriver extends MapReduceStitchingDriver {

    private String hdfsInputPath = null;

    private String hdfsSourceImage = null;
    private String hdfsTargetImage = null;

    private String threshold = null;

    private String localOutputPath = null;

    public ImageDataClusteringDriver() {
        super(ImageDataClusteringDriver.class);
    }

    /**
     * Copies all the local files to HDFS required by the mapreduce job
     */
    public void setup(ConfigurationParams params) throws HdfsException {
        super.setup(params);

        this.localOutputPath = params.getParam("local.output.path", null);
        threshold = params.getParam("error.threshold", null);

        String localSourceImage = params.getParam("local.image.source.file", null);
        String localTargetImage = params.getParam("local.image.target.file", null);

        logger.debug("Copying all local files to HDFS dir - " + hdfsBaseDirPath);
        hdfsInputPath = HdfsFileUtils.copyFromLocal(params.getParam("local.input.path", null),
                                                    hdfsBaseDirPath,
                                                    conf);
        hdfsSourceImage = HdfsFileUtils.copyFromLocal(localSourceImage, hdfsBaseDirPath, conf);
        hdfsTargetImage = HdfsFileUtils.copyFromLocal(localTargetImage, hdfsBaseDirPath, conf);

        logger.info("Image stitching mapreduce job setup successful.");
    }

    /**
     * Copies all required libraries to mapreduce job class path. Kicks off the
     * mapreduce job. Every input line specifies configurations defined for
     * clustering data points.
     */
    public boolean run() throws HdfsException {

        try {
            Job job = createJobInstance();
            job.setMapperClass(ImageDataClusteringMapper.class);

            job.getConfiguration().setInt("mapreduce.input.lineinputformat.linespermap", 3);
            job.getConfiguration().set(HdfsConstants.SOURCE_IMAGE_HDFS_PATH, hdfsSourceImage);
            job.getConfiguration().set(HdfsConstants.TARGET_IMAGE_HDFS_PATH, hdfsTargetImage);
            job.getConfiguration().set(HdfsConstants.IMAGE_MATCHING_ERROR, threshold);

            // add all required jars to mapreduce job
            addJarsToDistributedCache(job);

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
                logger.info("Mapreduce job with id ["
                            + job.getJobID()
                            + "] completed successfully.");
                HdfsFileUtils.copyToLocal(hdfsOutputPath, localOutputPath, conf);
                logger.info("HDFS output files copied to local file system at location - "
                            + new File(localOutputPath).getAbsolutePath());
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
}

