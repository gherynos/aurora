package co.naes.aurora;

public class AuroraException extends Exception {

    private static final long serialVersionUID = 837592834362474L;

    public AuroraException(String message) {

        super(message);
    }

    public AuroraException(String message, Throwable cause) {

        super(message, cause);
    }
}
