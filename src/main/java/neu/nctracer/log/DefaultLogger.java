package neu.nctracer.log;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Default logger implementation for {@linkplain neu.nctracer.log.Logger}
 * 
 * @author Ankur Shanbhag
 *
 */
class DefaultLogger implements neu.nctracer.log.Logger {
    private Logger logger;

    DefaultLogger(URL url) {
        DOMConfigurator.configure(url);
        logger = Logger.getLogger(DefaultLogger.class);
    }

    @Override
    public void debug(String message) {
        logger.debug(constructMessage(message));
    }

    @Override
    public void info(String message) {
        logger.info(constructMessage(message));
    }

    @Override
    public void info(String message, Throwable t) {
        logger.info(constructMessage(message), t);
    }

    @Override
    public void warn(String message) {
        logger.warn(constructMessage(message));

    }

    @Override
    public void warn(String message, Throwable t) {
        logger.warn(constructMessage(message), t);
    }

    @Override
    public void error(String message) {
        logger.error(constructMessage(message));
    }

    @Override
    public void error(String message, Throwable t) {
        logger.error(constructMessage(message), t);

    }

    @Override
    public void fatal(String message, Throwable t) {
        logger.fatal(constructMessage(message), t);
    }

    private String constructMessage(String message) {
        return getCallerInfo() + " | " + message;
    }

    private String getCallerInfo() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        int i = 0;
        String thisClassName = DefaultLogger.class.getName();
        while (i < stackTraceElements.length
                && !thisClassName.equals(stackTraceElements[i].getClassName())) {
            i++;
        }

        // this logger class found. Need to find class which called this logger
        while (i < stackTraceElements.length
                && thisClassName.equals(stackTraceElements[i].getClassName())) {
            i++;
        }

        // caller found
        return (stackTraceElements.length <= i) ? "UNKNOWN" : stackTraceElements[i].toString();
    }
}