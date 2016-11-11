package neu.nctracer.log;

import org.apache.log4j.Logger;

/**
 * Logger class to be used by all the classes part of map-reduce program
 * 
 * @author Ankur Shanbhag
 *
 */
public class MapReducerLogger extends AbstractLogger {

    private final Logger logger;

    MapReducerLogger() {
        super(MapReducerLogger.class);
        logger = Logger.getLogger(MapReducerLogger.class);
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
}
