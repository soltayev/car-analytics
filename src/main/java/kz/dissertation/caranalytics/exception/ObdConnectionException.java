package kz.dissertation.caranalytics.exception;

public class ObdConnectionException extends RuntimeException {

    public ObdConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObdConnectionException(String message) {
        super(message);
    }
}
