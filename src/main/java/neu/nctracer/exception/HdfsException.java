package neu.nctracer.exception;

public class HdfsException extends Exception {

	private static final long serialVersionUID = 83247912381273L;

	private String message;
	private Throwable cause;

	public HdfsException(String message) {
		super(message);
		this.message = message;
	}

	public HdfsException(String message, Throwable cause) {
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