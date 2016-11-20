package neu.nctracer.mr;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import neu.nctracer.data.DataObject;
import neu.nctracer.data.DataTransformation;
import neu.nctracer.data.Match;
import neu.nctracer.dm.MatchCalculator;
import neu.nctracer.dm.TranslationMatchCalculator;
import neu.nctracer.dm.conf.DMConfigurationHandler;
import neu.nctracer.exception.ParsingException;
import neu.nctracer.exception.ReflectionUtilsException;
import neu.nctracer.utils.ReflectionUtils;

/**
 * Applies all input transformation to all the source data points and computes
 * correspondences with matching error
 * 
 * @author Ankur Shanbhag
 *
 */
public class PointToPointTranslationMapper
        extends ImageStitchingMapper<LongWritable, Text, Text, NullWritable> {

    private MatchCalculator matchCalculator = null;

    @Override
    protected void
              setup(Mapper<LongWritable, Text, Text, NullWritable>.Context context) throws IOException,
                                                                                    InterruptedException {
        super.setup(context);
        Collection<DataObject> sourceDataObjects = getSourceDataObjects();
        Collection<DataObject> targetDataObjects = getTargetDataObjects();

        // place target image data points to the right of source data points
        placeSidebySide(sourceDataObjects, targetDataObjects);
        this.matchCalculator = getMatchingCalculationClass(conf);
        this.matchCalculator.setup(DMConfigurationHandler.getHandler()
                                                         .getConfigurationParamsInstance());
    }

    private MatchCalculator getMatchingCalculationClass(Configuration conf) {
        MatchCalculator defaultInstance = new TranslationMatchCalculator();

        String className = conf.get("match.calculator.class");
        if (null == className)
            return defaultInstance;

        try {
            return ReflectionUtils.instantiate(className, MatchCalculator.class);
        } catch (ReflectionUtilsException e) {
            logger.warn("Class [ "
                        + className
                        + "] specified as parameter to [match.calculator.class] cannot be instantiated");
            return defaultInstance;
        }
    }

    @Override
    protected void
              map(LongWritable key,
                  Text value,
                  Mapper<LongWritable, Text, Text, NullWritable>.Context context) throws IOException,
                                                                                  InterruptedException {
        try {
            DataTransformation<?> transformation = DataTransformation.parse(value.toString());
            Match match = matchCalculator.findMatch(getSourceDataObjects(),
                                                    getTargetDataObjects(),
                                                    transformation);
            if (match.getCorrespondences() == null || match.getCorrespondences().isEmpty())
                return;

            context.write(new Text(match.getCorrespondences().toString()), NullWritable.get());
        } catch (ParsingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Computes max coordinates for source images and adds them to the target
     * image data. This will keep 2 images places side by side without any gap
     * between them
     * 
     * @param sourceDataObjects
     * @param targetDataObjects
     */
    private void placeSidebySide(Collection<DataObject> sourceDataObjects,
                                 Collection<DataObject> targetDataObjects) {
        double[] max = computeMax(sourceDataObjects);
        for (DataObject obj : targetDataObjects) {
            double[] features = obj.getFeatures();
            for (int i = 0; i < obj.getDimension(); i++) {
                features[i] += max[i];
            }
        }
    }

    private double[] computeMax(Collection<DataObject> data) {
        double[] max = null;

        for (DataObject obj : data) {
            if (null == max) {
                max = new double[obj.getDimension()];
                Arrays.fill(max, Double.MIN_VALUE);
            }

            for (int i = 0; i < max.length; i++) {
                if (max[i] < obj.getFeatures()[i])
                    max[i] = obj.getFeatures()[i];
            }
        }
        return max;
    }
}
