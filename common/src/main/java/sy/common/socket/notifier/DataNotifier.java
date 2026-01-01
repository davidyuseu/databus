package sy.common.socket.notifier;

import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.DataUnit;

import java.util.Queue;

/**
 * 接收到数据时，会触发此接口的更新方法
 */
public interface DataNotifier {
    void onDataUnitUpdate(String key, DataUnit value);

    void onQueueUpdate(String key, Queue<DataUnit> value);
}
