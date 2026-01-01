package sy.databus.process;

public class ClosingException extends Exception {
    public ClosingException() {
    }

    public ClosingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClosingException(String message) {
        super(message);
    }

    public ClosingException(Throwable cause) {
        super(cause);
    }
}
