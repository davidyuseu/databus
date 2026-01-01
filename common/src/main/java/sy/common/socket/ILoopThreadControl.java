package sy.common.socket;

public interface ILoopThreadControl {
    void goRunning();
    void stopRunning();
    boolean getRunning();
}
