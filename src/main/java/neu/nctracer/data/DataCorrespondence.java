package neu.nctracer.data;

/**
 * Class is used for holding one-to-one correspondence between two
 * {@linkplain DataObject}
 * 
 * @author Ankur Shanbhag
 *
 */
public class DataCorrespondence {

    private DataObject source;
    private DataObject target;

    public DataCorrespondence(DataObject source, DataObject target) {
        this.source = source;
        this.target = target;
    }

    public void setCorrespondence(DataObject source, DataObject target) {
        this.source = source;
        this.target = target;
    }

    public DataObject getSource() {
        return source;
    }

    public DataObject getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 1;
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
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
        DataCorrespondence other = (DataCorrespondence) obj;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(source);
        builder.append(",");
        builder.append(target);
        return builder.toString();
    }

}
