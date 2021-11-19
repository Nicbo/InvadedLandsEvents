package ca.nicbo.invadedlandsevents.exception;

/**
 * Thrown when the version is unsupported.
 *
 * @author Nicbo
 */
public class UnsupportedVersionException extends RuntimeException {
    public UnsupportedVersionException() {
        super();
    }

    public UnsupportedVersionException(String message) {
        super(message);
    }

    public UnsupportedVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedVersionException(Throwable cause) {
        super(cause);
    }
}
