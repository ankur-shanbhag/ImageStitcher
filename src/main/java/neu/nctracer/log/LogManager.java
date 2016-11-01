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
        URL url = LogManager.class.getResource("/log4j.xml");
        defaultLogger = new DefaultLogger(url);
    }

    public static LogManager getLogManager() {
        return logManager;
    }

    public void setDefaultLogger(Logger logger) {
        this.defaultLogger = logger;
    }

    public Logger getDefaultLogger() {
        return defaultLogger;
    }
}