package sy.common.socket.uplink;

import sy.common.socket.DynamicSocketRunner;
import sy.common.socket.ILoopThreadControl;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.UplinkDataHolder;
import sy.common.socket.global.DataUtil;
import sy.common.socket.global.GlobalDataHolder;

public class DataUnitLooper extends Thread implements ILoopThreadControl {
    private BaseUplink uplink;
    private UplinkDataHolder uplinkDataHolder;

    DataUnitLooper(BaseUplink uplink, UplinkDataHolder uplinkDataHolder,String socketConfigName) {
        this.uplink = uplink;
        this.uplinkDataHolder = uplinkDataHolder;
        // 初始化后，放入dataMap
        DataUnit dataUnit = DataUtil.assembleDataUnit(uplinkDataHolder);
        GlobalDataHolder.dataMap.put(uplinkDataHolder.getKey(), dataUnit);
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
        while (running) {
            DataUnit unit = GlobalDataHolder.dataMap.get(uplinkDataHolder.getKey());
            // 保证至少发送一次
            uplink.joinChecksAndSendDataUnit(unit, uplinkDataHolder);
        }
    }
}
