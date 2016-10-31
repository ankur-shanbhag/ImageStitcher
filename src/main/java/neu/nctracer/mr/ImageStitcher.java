package neu.nctracer.mr;

import java.io.IOException;

import neu.nctracer.exception.HdfsException;

/**
 * Interface to be implemented by all the classes performing image stitching
 * task
 * 
 * @author Ankur Shanbhag
 *
 */
public interface ImageStitcher {

    void setup(String localInputPath, String localOutputPath) throws IOException;

    boolean run() throws HdfsException;
}
