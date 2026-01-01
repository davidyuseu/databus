package sy.databus.process.frame.handler.decoder;

public class CodecException extends RuntimeException {

    public CodecException() {
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodecException(String message) {
        super(message);
    }

    public CodecException(Throwable cause) {
        super(cause);
    }
}
