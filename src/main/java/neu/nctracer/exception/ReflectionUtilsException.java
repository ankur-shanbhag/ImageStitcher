package neu.nctracer.exception;

public class ReflectionUtilsException extends Exception {
    private static final long serialVersionUID = 2232332434265L;

    private String message;
    private Throwable cause;

    public ReflectionUtilsException(String message) {
        super(message);
        this.message = message;
    }

    public ReflectionUtilsException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

    public ReflectionUtilsException(Throwable cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
