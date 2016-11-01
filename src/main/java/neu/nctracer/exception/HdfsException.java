package neu.nctracer.exception;

import java.io.IOException;

public class HdfsException extends IOException {

    private static final long serialVersionUID = 533247912381273L;

    private String message;

    public HdfsException(String message) {
        super(message);
        this.message = message;
    }

    public HdfsException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    public HdfsException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
