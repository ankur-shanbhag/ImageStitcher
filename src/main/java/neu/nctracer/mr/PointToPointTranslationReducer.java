package neu.nctracer.mr;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Reducer;

import neu.nctracer.data.Match;

/**
 * Reducer to emit just keys for every reduce call.
 * 
 * @author Ankur Shanbhag
 *
 */
public class PointToPointTranslationReducer
        extends Reducer<Match, NullWritable, Match, NullWritable> {

    @Override
    protected void
              reduce(Match match,
                     Iterable<NullWritable> value,
                     Reducer<Match, NullWritable, Match, NullWritable>.Context context) throws IOException,
                                                                                        InterruptedException {
        context.write(match, NullWritable.get());
    }

    /**
     * Groups all the records together in a single reduce call
     * 
     * @author Ankur Shanbhag
     *
     */
    public static class PointToPointTranslationGroupComparator extends WritableComparator {
        public PointToPointTranslationGroupComparator() {
            super(Match.class, true);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public int compare(WritableComparable w1, WritableComparable w2) {
            // Group all the records in a single reduce call
            return 0;
        }
    }
}
