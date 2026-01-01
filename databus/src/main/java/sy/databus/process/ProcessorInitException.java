package sy.databus.process;

public class ProcessorInitException extends RuntimeException{
    public ProcessorInitException() {
    }

    public ProcessorInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessorInitException(String message) {
        super(message);
    }

    public ProcessorInitException(Throwable cause) {
        super(cause);
    }
}
