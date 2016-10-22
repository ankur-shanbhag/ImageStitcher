package neu.nctracer.mr;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

import neu.nctracer.exception.HdfsException;

/**
 * Interface to be implemented by all the classes performing image stitching task
 * 
 * @author Ankur Shanbhag
 *
 */
public interface ImageStitcher {

    void setup(Configuration conf, String localInputPath, String localOutputPath)
            throws IOException;

    boolean run() throws HdfsException;
}