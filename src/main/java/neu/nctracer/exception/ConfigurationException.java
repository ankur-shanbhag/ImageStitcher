package neu.nctracer.exception;

public class ConfigurationException extends Exception {

    private static final long serialVersionUID = 648468276291901L;

    private String message;

    public ConfigurationException(String message) {
        super(message);
        this.message = message;
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}