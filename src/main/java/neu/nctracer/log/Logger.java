package neu.nctracer.log;

/**
 * Interface to be implemented by all the custom logger classes
 * 
 * @author Ankur Shanbhag
 */
public interface Logger {

    void debug(String message);

    void info(String message);

    void info(String message, Throwable t);

    void warn(String message);

    void warn(String message, Throwable t);

    void error(String message);

    void error(String message, Throwable t);

    void fatal(String message, Throwable t);
}