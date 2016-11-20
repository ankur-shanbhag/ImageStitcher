package neu.nctracer.mr;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import neu.nctracer.data.DataObject;
import neu.nctracer.data.DataTransformation;
import neu.nctracer.data.ImageData;
import neu.nctracer.dm.TranslationMatchCalculator;
import neu.nctracer.dm.conf.ConfigurationParams;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.exception.ParsingException;
import neu.nctracer.utils.DataParser;
import neu.nctracer.utils.DataTransformer;
import neu.nctracer.utils.HdfsFileUtils;

public class PointToPointTranslationDriver extends MapReduceStitchingDriver {

    private String hdfsInputPath = null;
    private ConfigurationParams params = null;

    public PointToPointTranslationDriver() {
        super(PointToPointTranslationDriver.class);
    }

    @Override
    public void setup(ConfigurationParams params) throws HdfsException {
        super.setup(params);

        this.params = params;

        String sourceImageFile = params.getParam("local.image.source.file", null);
        String targetImageFile = params.getParam("local.image.target.file", null);

        String sourceDataAsString = HdfsFileUtils.readFileAsString(conf, sourceImageFile, false);
        String targetDataAsString = HdfsFileUtils.readFileAsString(conf, targetImageFile, false);

        try {
            List<DataObject> sourceData = DataParser.parseData(sourceDataAsString, ImageData.class);
            List<DataObject> targetData = DataParser.parseData(targetDataAsString, ImageData.class);

            this.hdfsInputPath = hdfsBaseDirPath + Path.SEPARATOR + "input-file";
            writeTranslationsToHdfs(sourceData, targetData);
        } catch (ParsingException e) {
            throw new HdfsException("Error while parsing image data.", e);
        }

        logger.info("Image stitching mapreduce job setup successful.");
    }

    private void writeTranslationsToHdfs(List<DataObject> sourceData,
                                         List<DataObject> targetData) throws HdfsException {
        BufferedWriter writer = null;
        int count = 0;
        Random r = new Random(System.currentTimeMillis());
        try {
            FileSystem fs = HdfsFileUtils.getFileSystem(conf, hdfsInputPath, true);
            writer = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(hdfsInputPath))));
            for (DataObject source : sourceData) {
                for (DataObject target : targetData) {
                    if (r.nextDouble() < 0.7)
                        continue;
                    double distance = DataTransformer.computeEuclideanDistance(source, target);
                    double[] angles = DataTransformer.computeDirectionAngles(source, target);

                    DataTransformation<DataObject> transformation = new DataTransformation<>();
                    transformation.setDistance(distance);
                    transformation.setAngles(angles);
                    // write all the transformations to HDFS
                    writer.write(transformation.toString());
                    writer.newLine();
                    if (++count == 200)
                        return;
                }
            }
        } catch (IOException exp) {
            throw new HdfsException(exp);
        } finally {
            if (null != writer) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException exp) {
                    // Ignore
                }
            }
        }
    }

    @Override
    public boolean run() throws HdfsException {
        try {
            Job job = createJobInstance();
            job.setMapperClass(ImageDataClusteringMapper.class);

            setJobConfigurations(job);

            job.setMapperClass(MultithreadedMapper.class);
            MultithreadedMapper.setMapperClass(job, PointToPointTranslationMapper.class);
            int numThreadsPerMapper = Integer.parseInt(params.getParam("num.threads.per.mapper",
                                                                       "4"));
            MultithreadedMapper.setNumberOfThreads(job, numThreadsPerMapper);

            // map only job
            job.setNumReduceTasks(0);

            // add all required jars to mapreduce job
            addJarsToDistributedCache(job);

            NLineInputFormat.addInputPath(job, new Path(hdfsInputPath));
            job.setInputFormatClass(NLineInputFormat.class);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(NullWritable.class);

            FileOutputFormat.setOutputPath(job, new Path(hdfsOutputPath));

            logger.info("Starting mapreduce job to perform image stitching operation.");
            boolean status = job.waitForCompletion(true);
            cleanup(job, status);

            return status;

        } catch (IOException e) {
            throw new HdfsException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new HdfsException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new HdfsException(e.getMessage(), e);
        }
    }

    private void setJobConfigurations(Job job) {
        int inputLinesPerMapper = Integer.parseInt(params.getParam("num.input.lines.mapper",
                                                                   "1000"));
        job.getConfiguration().setInt("mapreduce.input.lineinputformat.linespermap",
                                      inputLinesPerMapper);
        logger.debug("Setting input lines per mapper to " + inputLinesPerMapper);

        job.getConfiguration().set(HdfsConstants.SOURCE_IMAGE_HDFS_PATH, hdfsSourceImagePath);
        job.getConfiguration().set(HdfsConstants.TARGET_IMAGE_HDFS_PATH, hdfsTargetImagePath);

        String matchCalculationClass = params.getParam("match.calculator.class",
                                                       TranslationMatchCalculator.class.getName());
        job.getConfiguration().set("match.calculator.class", matchCalculationClass);
        logger.debug("Setting match calculator class to ["
                     + matchCalculationClass
                     + "]. The class can be configured with parameter [match.calculator.class]");
    }

}
