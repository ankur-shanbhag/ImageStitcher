package neu.nctracer.log;

/**
 * Provides features useful for implementing different logger classes
 * 
 * @author Ankur Shanbhag
 *
 */
public abstract class AbstractLogger implements Logger {

    private final Class<? extends AbstractLogger> loggerClass;

    public AbstractLogger(Class<? extends AbstractLogger> loggerClass) {
        this.loggerClass = loggerClass;
    }

    protected String constructMessage(String message) {
        return getCallerInfo() + " | " + message;
    }

    /**
     * Fetches caller info for the logger statement from the stack trace
     */
    protected String getCallerInfo() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        int i = 0;
        String thisClassName = loggerClass.getName();
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
