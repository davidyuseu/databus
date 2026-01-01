package sy.common.socket.uplink;

import sy.common.socket.DynamicSocketRunner;
import sy.common.socket.ILoopThreadControl;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.UplinkDataHolder;
import sy.common.socket.global.GlobalDataHolder;
import sy.common.socket.DynamicSocketRunner;
import sy.common.socket.ILoopThreadControl;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.UplinkDataHolder;
import sy.common.socket.global.GlobalDataHolder;

import java.util.LinkedList;
import java.util.Queue;

public class DataQueueLooper extends Thread implements ILoopThreadControl {
    private BaseUplink uplink;
    private UplinkDataHolder uplinkDataHolder;

    DataQueueLooper(BaseUplink uplink, UplinkDataHolder uplinkDataHolder, String socketConfigName) {
        this.uplink = uplink;
        this.uplinkDataHolder = uplinkDataHolder;
        GlobalDataHolder.queueMap.put(uplinkDataHolder.getKey(), new LinkedList<>());
        DynamicSocketRunner.loopThreadMap.put(socketConfigName,this);
    }

    private volatile boolean running = true;

    @Override
    public synchronized void goRunning() {
        running = true;
    }

    @Override
    public synchronized void stopRunning() {
        running = false;
    }

    @Override
    public synchronized boolean getRunning() {
        return running;
    }

    @Override
    public void run() {
        Queue<DataUnit> queue = GlobalDataHolder.queueMap.get(uplinkDataHolder.getKey());
        while (running) {
            if (queue != null && queue.size() > 0) {
                DataUnit unit = queue.poll();
                uplink.joinChecksAndSendDataUnit(unit, uplinkDataHolder);
            }
        }
    }
}
