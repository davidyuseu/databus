package sy.common.socket.uplink;

import sy.common.socket.entity.CheckConfig;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.UplinkConfig;
import sy.common.socket.entity.UplinkDataHolder;
import sy.common.socket.global.Constants;
import sy.common.socket.global.DataUtil;
import sy.common.socket.global.GlobalDataHolder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class TCPSendThread extends Thread implements BaseUplink {
    private UplinkConfig config;
    private Socket socket;

    TCPSendThread(UplinkConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        try {
            // 绑定到本机ip
            InetAddress localAddr = InetAddress.getByName(config.getLocalIp());
            socket = new Socket(config.getAddr(), config.getPort(), localAddr, 0);
            System.out.println("创建" + config.getName() + "成功");
        } catch (IOException e) {
            System.err.println("上行：创建tcp发送线程失败：" + config.getName());
            System.err.println("message：" + e.getMessage());
            return;
        }

        for (UplinkDataHolder uplinkDataHolder : config.getData()) {
            if (uplinkDataHolder.getDataType().equals(Constants.DATA_UNIT)) {
               // 如果是data unit，则初始化后，放入dataMap
                new DataUnitLooper(this, uplinkDataHolder,config.getName()).start();
            } else if (uplinkDataHolder.getDataType().equals(Constants.DATA_QUEUE)) {
                new DataQueueLooper(this, uplinkDataHolder,config.getName()).start();
            }
        }
    }

    @Override
    public void joinChecksAndSendDataUnit(DataUnit unit, List<CheckConfig> checks) {
        DataUtil.joinChecks(unit, checks);
        try {
            socket.getOutputStream().write(unit.bytes);
            // 每发送一次，就增加一次计数
            GlobalDataHolder.increaseCount(config.getName());

        } catch (IOException e) {
            System.err.println("tcp发送数据失败：" + config.getName());
            System.err.println("message：" + e.getMessage());
        }
    }

    @Override
    public void joinChecksAndSendDataUnit(DataUnit unit, UplinkDataHolder dataHolder) {
        DataUtil.joinChecks(unit, dataHolder.getChecks());
        try {
//            // 每发送一次，就增加一次计数
            int count = 1;
            long interval = 0;
            if(unit.count!=null&&unit.count>1) {
                count = unit.count;
            }else {
                if(dataHolder.getCount()!=null&&dataHolder.getCount()>1){
                    count = dataHolder.getCount();
                }
            }

            if(unit.interval!=null&&unit.interval>0){
                interval = unit.interval;
            }else {
                if(dataHolder.getInterval()!=null&&dataHolder.getInterval()>0){
                    interval = dataHolder.getInterval();
                }
            }

            for(int i = 0; i < count-1; ++i){//注意count要减1，因为最后一次发送之后不需要延时
                socket.getOutputStream().write(unit.bytes);
                GlobalDataHolder.increaseCount(config.getName());
                try {
                    if(interval>0)
                        Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    System.err.println("发送线程异常, key: " + dataHolder.getKey());
                }
            }
            //最后一次
            socket.getOutputStream().write(unit.bytes);
            GlobalDataHolder.increaseCount(config.getName());
        } catch (IOException e) {
            System.err.println("tcp发送数据失败：" + config.getName());
            System.err.println("message：" + e.getMessage());
        }
    }
}