package neu.nctracer.exception;

public class InvalidConfigKeyException extends Exception {
    private static final long serialVersionUID = 96443652423847L;

    private String message;

    public InvalidConfigKeyException(String message) {
        super(message);
    }

    public InvalidConfigKeyException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public InvalidConfigKeyException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}