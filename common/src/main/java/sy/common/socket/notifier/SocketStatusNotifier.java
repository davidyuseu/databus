package sy.common.socket.notifier;

public interface SocketStatusNotifier {

    /**
     * socket通断状态发生改变
     *
     * @param key   socket名称
     * @param alive 状态
     */
    void onSocketStatusUpdate(String key, boolean alive);

    /**
     * socket 发送或接收数据时，触发此方法
     *
     * @param key   socket名称
     * @param count 共接收/发送了多少条数据
     */
    void onDataCountUpdate(String key, int count);
}
