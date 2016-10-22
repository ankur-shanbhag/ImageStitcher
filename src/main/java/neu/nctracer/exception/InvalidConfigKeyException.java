package neu.nctracer.exception;

public class InvalidConfigKeyException extends Exception {
    private static final long serialVersionUID = 964439832423847L;

    private String message;
    private Throwable cause;

    public InvalidConfigKeyException(String message) {
        super(message);
        this.message = message;
    }

    public InvalidConfigKeyException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

    public InvalidConfigKeyException(Throwable cause) {
        super(cause);
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