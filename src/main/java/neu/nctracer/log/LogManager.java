package neu.nctracer.log;

import java.net.URL;

import neu.nctracer.conf.ConfigurationManager;
import neu.nctracer.exception.ConfigurationException;

public class LogManager {

    private static LogManager logManager;

    private GenericLogger logger;

    private LogManager() throws ConfigurationException {
        URL url = ConfigurationManager.class.getResource("/log4j.xml");
        logger = new DefaultLogger(url);
    }

    public static LogManager getLogManager() throws ConfigurationException {
        if (null == logManager) {
            synchronized (LogManager.class) {
                // double checked locking to ensure single instance is created
                if (null == logManager)
                    logManager = new LogManager();
            }
        }

        return logManager;
    }

    public void setDefaultLogger(GenericLogger logger) {
        this.logger = logger;
    }

    public GenericLogger getDefaultLogger() {
        return logger;
    }
}
