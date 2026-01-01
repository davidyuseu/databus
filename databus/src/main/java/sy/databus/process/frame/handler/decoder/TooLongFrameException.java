package sy.databus.process.frame.handler.decoder;


public class TooLongFrameException extends DecoderException {

    private static final long serialVersionUID = -1995801950698951640L;


    public TooLongFrameException() {
    }

    public TooLongFrameException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooLongFrameException(String message) {
        super(message);
    }

    public TooLongFrameException(Throwable cause) {
        super(cause);
    }
}