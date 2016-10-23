package neu.nctracer.exception;

public class ParsingException extends Exception {

	private static final long serialVersionUID = 35734874387534L;

	private String message;
	private Throwable cause;

	public ParsingException(String message) {
		super(message);
		this.message = message;
	}

	public ParsingException(String message, Throwable cause) {
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
