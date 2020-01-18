package co.naes.aurora;

public class AuroraException extends Exception {

    public AuroraException(String message) {

        super(message);
    }

    public AuroraException(String message, Throwable cause) {

        super(message, cause);
    }
}
