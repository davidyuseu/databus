package sy.databus.entity.property;

public class PropertyException extends Exception {
    private static final long serialVersionUID = 7830266012832686112L;

    public PropertyException() { }

    public PropertyException(String message) {
        super(message);
    }

    public PropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyException(Throwable cause) {
        super(cause);
    }

}
