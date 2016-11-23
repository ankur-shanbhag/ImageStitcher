package neu.nctracer.mr;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

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
        extends ImageStitchingMapper<LongWritable, Text, Match, NullWritable> {

    private MatchCalculator matchCalculator = null;
    private Match bestLocalMatch = null;

    @Override
    protected void
              setup(Mapper<LongWritable, Text, Match, NullWritable>.Context context) throws IOException,
                                                                                     InterruptedException {
        super.setup(context);

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
                  Mapper<LongWritable, Text, Match, NullWritable>.Context context) throws IOException,
                                                                                   InterruptedException {
        try {
            DataTransformation<?> transformation = DataTransformation.parse(value.toString());
            Match match = matchCalculator.findMatch(getSourceDataObjects(),
                                                    getTargetDataObjects(),
                                                    transformation);
            if (match.getCorrespondences() == null || match.getCorrespondences().isEmpty())
                return;

            // keep track of local best scoring match
            if (null == bestLocalMatch || match.getScore() > bestLocalMatch.getScore())
                bestLocalMatch = match;
        } catch (ParsingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void
              cleanup(Mapper<LongWritable, Text, Match, NullWritable>.Context context) throws IOException,
                                                                                       InterruptedException {
        if (null != bestLocalMatch)
            context.write(bestLocalMatch, NullWritable.get());

        super.cleanup(context);
    }
}

