package neu.nctracer.exception;

public class ParsingException extends Exception {

	private static final long serialVersionUID = 357385764387534L;

	private String message;

	public ParsingException(String message) {
		super(message);
		this.message = message;
	}

	public ParsingException(String message, Throwable cause) {
		super(message, cause);
		this.message = message;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
