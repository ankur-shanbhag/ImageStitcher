package neu.nctracer.mr;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.NLineInputFormat;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.data.DataObject;
import neu.nctracer.data.DataTransformation;
import neu.nctracer.data.ImageData;
import neu.nctracer.data.Match;
import neu.nctracer.dm.TranslationMatchCalculator;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.exception.ParsingException;
import neu.nctracer.mr.PointToPointTranslationReducer.PointToPointTranslationGroupComparator;
import neu.nctracer.utils.DataParser;
import neu.nctracer.utils.DataTransformer;
import neu.nctracer.utils.HdfsFileUtils;

/**
 * Driver class to invoke a map-reduce job which can stitch images based on
 * translation defined between any pair of source and target image points
 * 
 * @author Ankur Shanbhag
 *
 */
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
        try {
            FileSystem fs = HdfsFileUtils.getFileSystem(conf, hdfsInputPath, true);
            writer = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(hdfsInputPath))));
            for (DataObject source : sourceData) {
                for (DataObject target : targetData) {
                    double distance = DataTransformer.computeEuclideanDistance(source, target);
                    double[] angles = DataTransformer.computeDirectionAngles(source, target);

                    DataTransformation<DataObject> transformation = new DataTransformation<>();
                    transformation.setDistance(distance);
                    transformation.setAngles(angles);
                    // write all the transformations to HDFS
                    writer.write(transformation.toString());
                    writer.newLine();
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

            // add image files to distributed cache
            addImageFilesToCache(job);

            // add all required jars to mapreduce job
            addJarsToDistributedCache(job);

            setMapperConfigurations(job);

            // Reduce phase configurations
            job.setGroupingComparatorClass(PointToPointTranslationGroupComparator.class);
            job.setReducerClass(PointToPointTranslationReducer.class);
            job.setNumReduceTasks(1);

            NLineInputFormat.addInputPath(job, new Path(hdfsInputPath));
            job.setInputFormatClass(NLineInputFormat.class);

            job.setOutputKeyClass(Match.class);
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
        } catch (URISyntaxException e) {
            throw new HdfsException(e);
        }
    }

    private void setMapperConfigurations(Job job) {
        // using multi-threaded mapper
        job.setMapperClass(MultithreadedMapper.class);
        MultithreadedMapper.setMapperClass(job, PointToPointTranslationMapper.class);

        int numThreadsPerMapper = Integer.parseInt(params.getParam("num.threads.per.mapper", "2"));
        MultithreadedMapper.setNumberOfThreads(job, numThreadsPerMapper);

        // output types from Mapper
        job.setMapOutputKeyClass(Match.class);
        job.setMapOutputValueClass(NullWritable.class);

        int inputLinesPerMapper = Integer.parseInt(params.getParam("num.input.lines.mapper",
                                                                   "1000"));
        job.getConfiguration().setInt("mapreduce.input.lineinputformat.linespermap",
                                      inputLinesPerMapper);
        logger.debug("Setting input lines per mapper to " + inputLinesPerMapper);
    }

    private void setJobConfigurations(Job job) {
        String matchCalculationClass = params.getParam("match.calculator.class",
                                                       TranslationMatchCalculator.class.getName());
        job.getConfiguration().set("match.calculator.class", matchCalculationClass);
        logger.debug("Setting match calculator class to ["
                     + matchCalculationClass
                     + "]. The class can be configured with parameter [match.calculator.class]");
    }

}

