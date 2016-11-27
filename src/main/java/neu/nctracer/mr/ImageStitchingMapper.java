package neu.nctracer.mr;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

import neu.nctracer.data.DataObject;
import neu.nctracer.data.ImageData;
import neu.nctracer.dm.conf.ConfigurationParams;
import neu.nctracer.dm.conf.DMConfigurationHandler;
import neu.nctracer.exception.HdfsException;
import neu.nctracer.exception.ParsingException;
import neu.nctracer.log.LogManager;
import neu.nctracer.log.Logger;
import neu.nctracer.utils.DataParser;
import neu.nctracer.utils.HdfsFileUtils;

/**
 * Generic Mapper class to perform setup activity for mapreduce jobs
 * 
 * @author Ankur Shanbhag
 *
 * @param <IN_KEY>
 *            - Input key to the Mapper
 * @param <IN_VAL>
 *            - Input value to the Mapper
 * @param <OUT_KEY>
 *            - Output key emitted by Mapper
 * @param <OUT_VAL>
 *            - Output value emitted by Mapper
 */
public abstract class ImageStitchingMapper<IN_KEY, IN_VAL, OUT_KEY, OUT_VAL>
        extends Mapper<IN_KEY, IN_VAL, OUT_KEY, OUT_VAL> {

    protected Logger logger = null;

    private List<DataObject> sourceImageData = null;
    private List<DataObject> targetImageData = null;
    protected Configuration conf = null;

    protected ConfigurationParams params = null;

    /**
     * Sets up logger to be used and reads source and target image data files
     * in-memory for per stitching
     */
    @Override
    protected void
              setup(Mapper<IN_KEY, IN_VAL, OUT_KEY, OUT_VAL>.Context context) throws IOException,
                                                                              InterruptedException {
        super.setup(context);
        setDefaultLogger();

        this.conf = context.getConfiguration();

        try {
            parseConfigurableParams();
        } catch (ParsingException exp) {
            throw new HdfsException("Error parsing configuration parameters", exp);
        }

        String sourceFilePath = conf.get(HdfsConstants.SOURCE_IMAGE_HDFS_PATH);
        String sourceFileData = HdfsFileUtils.readFileAsString(conf, sourceFilePath, true);
        logger.debug("Successfully read source data file from location : " + sourceFilePath);

        String targetFilePath = conf.get(HdfsConstants.TARGET_IMAGE_HDFS_PATH);
        String targetFileData = HdfsFileUtils.readFileAsString(conf, targetFilePath, true);
        logger.debug("Successfully read target data file from location : " + targetFilePath);

        try {
            sourceImageData = DataParser.parseData(sourceFileData, ImageData.class);
            targetImageData = DataParser.parseData(targetFileData, ImageData.class);
        } catch (ParsingException e) {
            throw new HdfsException("Error while parsing image data.", e);
        }

    }

    protected void parseConfigurableParams() throws ParsingException {
        this.params = DMConfigurationHandler.getHandler().getConfigurationParamsInstance();
        String[] strings = this.conf.getStrings("configurable.params");
        if (null != strings)
            params.parseParams(strings);
    }

    protected void setDefaultLogger() {
        this.logger = LogManager.loggerInstance("mr");
        LogManager.getLogManager().setDefaultLogger(logger);
    }

    protected List<DataObject> getSourceDataObjects() {
        return sourceImageData;
    }

    protected List<DataObject> getTargetDataObjects() {
        return targetImageData;
    }
}

