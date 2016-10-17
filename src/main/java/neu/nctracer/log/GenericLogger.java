package neu.nctracer.log;

public interface GenericLogger {

    void debug(String message);

    void info(String message);

    void info(String message, Throwable t);

    void warn(String message);

    void warn(String message, Throwable t);

    void error(String message);

    void error(String message, Throwable t);

    void fatal(String message, Throwable t);
}
