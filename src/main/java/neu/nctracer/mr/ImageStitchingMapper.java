package neu.nctracer.mr;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

import neu.nctracer.conf.cli.CLIConfigurationManager;
import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.data.DataObject;
import neu.nctracer.data.ImageData;
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
            // read image data passed by distributed cache
            readImageData();
        } catch (ParsingException exp) {
            throw new HdfsException("Error parsing configuration parameters", exp);
        }
    }

    protected void readImageData() throws ParsingException, HdfsException {

        String sourceFileName = conf.get(HdfsConstants.SOURCE_IMAGE_FILE_NAME, null);
        String targetFileName = conf.get(HdfsConstants.TARGET_IMAGE_FILE_NAME, null);

        if (null == sourceFileName || null == targetFileName)
            throw new IllegalArgumentException("Missing image data files. Mapper requires mandatory params ["
                                               + HdfsConstants.SOURCE_IMAGE_FILE_NAME
                                               + "], ["
                                               + HdfsConstants.TARGET_IMAGE_FILE_NAME
                                               + "]");
        
        String sourceFileData = HdfsFileUtils.readFileAsString(conf, sourceFileName, false);
        String targetFileData = HdfsFileUtils.readFileAsString(conf, targetFileName, false);
        sourceImageData = DataParser.parseData(sourceFileData, ImageData.class);
        targetImageData = DataParser.parseData(targetFileData, ImageData.class);

        logger.info("Successfully read source and target image data files from distributed cache.");

    }

    protected void parseConfigurableParams() throws ParsingException {
        this.params = CLIConfigurationManager.getHandler().getConfigurationParamsInstance();
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

