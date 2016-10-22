package neu.nctracer.exception;

public class ConfigurationException extends Exception {

    private static final long serialVersionUID = 64473276291901L;

    private String message;
    private Throwable cause;

    public ConfigurationException(String message) {
        super(message);
        this.message = message;
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;

    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return ExceptionUtils.buildToString(message, cause);
    }
}