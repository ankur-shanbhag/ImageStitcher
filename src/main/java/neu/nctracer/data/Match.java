package neu.nctracer.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

import neu.nctracer.exception.ParsingException;

/**
 * Holds all the matching correspondences with overall matching score for the
 * match
 * 
 * @author Ankur Shanbhag
 *
 */
public class Match implements Comparable<Match>, WritableComparable<Match>, Writable {

    private Set<DataCorrespondence> correspondences;
    private double score;

    public Match() {
        // defined for sake of making it work as a Writable class
    }

    public Match(double score, Set<DataCorrespondence> correspondences) {
        this.score = score;
        this.correspondences = correspondences;
    }

    public Set<DataCorrespondence> getCorrespondences() {
        return correspondences;
    }

    public double getScore() {
        return score;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(score);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Match other = (Match) obj;
        if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
            return false;
        return true;
    }

    /**
     * Comparison based on the scores in descending order. If scores are equal,
     * checks for number of correspondences
     */
    @Override
    public int compareTo(Match other) {
        int result = -(Double.valueOf(this.getScore()).compareTo(Double.valueOf(other.getScore())));
        if (result == 0.0)
            return -(this.getCorrespondences().size() - other.getCorrespondences().size());

        return result;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeDouble(this.score);

        if (null == correspondences) {
            out.writeInt(0);
            return;
        }

        out.writeInt(correspondences.size());
        for (DataCorrespondence correspondence : correspondences) {
            out.writeUTF(correspondence.toString());
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.score = in.readDouble();

        int numCorrespondences = in.readInt();
        this.correspondences = new LinkedHashSet<>(numCorrespondences);

        while (numCorrespondences > 0) {
            try {
                this.correspondences.add(DataCorrespondence.parse(in.readUTF()));
            } catch (ParsingException e) {
                throw new IOException("Error parsing data correspondence.", e);
            }
            numCorrespondences--;
        }
    }

    @Override
    public String toString() {
        return "Score: " + this.score + ", Correspondences : " + correspondences;
    }
}

