package neu.nctracer.dm;

import java.util.List;

import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.data.DataObject;
import neu.nctracer.data.DataTransformation;
import neu.nctracer.data.Match;

/**
 * Interface to be implemented by all the classes which apply specified
 * transformation to all the source data points and computes correspondences
 * with match error
 * 
 * @author Ankur Shanbhag
 *
 */
public interface MatchCalculator {
    void setup(ConfigurationParams params);

    <T> Match findMatch(List<DataObject> source,
                        List<DataObject> target,
                        DataTransformation<T> transform);
}
