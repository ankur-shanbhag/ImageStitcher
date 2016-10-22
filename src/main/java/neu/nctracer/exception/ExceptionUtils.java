package neu.nctracer.exception;

public final class ExceptionUtils {

    public static String buildToString(String message, Throwable cause) {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("Exception occured while handling file. Actual msg [");
        msgBuilder.append(message);
        msgBuilder.append("]");

        if (null != cause) {
            msgBuilder.append(" Actual exception : ");
            msgBuilder.append(cause);
        }

        return msgBuilder.toString();
    }

    private ExceptionUtils() {
    }

}