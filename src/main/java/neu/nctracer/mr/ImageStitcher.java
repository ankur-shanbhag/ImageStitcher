package neu.nctracer.mr;

import org.apache.hadoop.conf.Configuration;

import neu.nctracer.exception.HdfsException;

public interface ImageStitcher {

    void setup(Configuration conf, String localInputPath, String localOutputPath)
            throws HdfsException;

    boolean run() throws HdfsException;
}
