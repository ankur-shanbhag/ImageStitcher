package neu.nctracer.log;

import java.net.URL;

/**
 * This class is responsible to provide default logger for all other classes.
 * Provides single point of control to configure default logger for the project.
 * <br>
 * Usage: <code>LogManager.getLogManager().getDefaultLogger()</code>
 * 
 * @author Ankur Shanbhag
 *
 */
public class LogManager {

    private static LogManager logManager = new LogManager();
    private Logger defaultLogger;

    private LogManager() {
    }

    public static LogManager getLogManager() {
        return logManager;
    }

    public static Logger createLogger(String type) {
        switch (type) {
        case "default":
            URL url = LogManager.class.getResource("/log4j.xml");
            Logger logger = new DefaultLogger(url);
            return logger;

        case "mr":
            Logger mapReducerLogger = new MapReducerLogger();
            return mapReducerLogger;
        }

        throw new IllegalArgumentException("Provided className [" + type + "]");
    }

    /**
     * Set default logger to be used by entire application
     * 
     * @param logger
     *            - logger to be set as default
     */
    public void setDefaultLogger(Logger logger) {
        this.defaultLogger = logger;
    }

    public Logger getDefaultLogger() {
        return this.defaultLogger;
    }
}

