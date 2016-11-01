package neu.nctracer.exception;

public class DataPlotException extends Exception {
    private static final long serialVersionUID = 74378438543589L;

    private String message;

    public DataPlotException() {
    }

    public DataPlotException(String message) {
        super(message);
    }

    public DataPlotException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public DataPlotException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
