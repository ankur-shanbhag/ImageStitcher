package neu.nctracer.exception;

public class DataPlotException extends Exception {
    private static final long serialVersionUID = 74378438543589L;

    private String message;
    private Throwable cause;

    public DataPlotException() {
    }

    public DataPlotException(String message) {
        super(message);
        this.message = message;
    }

    public DataPlotException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

    public DataPlotException(Throwable cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
