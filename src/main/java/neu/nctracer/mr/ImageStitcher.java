package neu.nctracer.mr;

import java.io.IOException;

import neu.nctracer.conf.cli.ConfigurationParams;
import neu.nctracer.exception.HdfsException;

/**
 * Interface to be implemented by all the classes performing image stitching
 * task.
 * 
 * @author Ankur Shanbhag
 *
 */
public interface ImageStitcher {

    void setup(ConfigurationParams params) throws IOException;

    boolean run() throws HdfsException;
}
