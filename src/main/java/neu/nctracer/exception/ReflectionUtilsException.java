package neu.nctracer.exception;

public class ReflectionUtilsException extends Exception {
    private static final long serialVersionUID = 222332332434265L;

    private String message;

    public ReflectionUtilsException(String message) {
        super(message);
        this.message = message;
    }

    public ReflectionUtilsException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public ReflectionUtilsException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
