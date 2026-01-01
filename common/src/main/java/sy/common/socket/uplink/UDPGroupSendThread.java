package sy.common.socket.uplink;

import sy.common.socket.Global_SocketLib;
import sy.common.socket.entity.CheckConfig;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.UplinkConfig;
import sy.common.socket.entity.UplinkDataHolder;
import sy.common.socket.global.Constants;
import sy.common.socket.global.DataUtil;
import sy.common.socket.global.GlobalDataHolder;
import sy.common.socket.Global_SocketLib;
import sy.common.socket.entity.CheckConfig;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.UplinkConfig;
import sy.common.socket.entity.UplinkDataHolder;
import sy.common.socket.global.Constants;
import sy.common.socket.global.DataUtil;
import sy.common.socket.global.GlobalDataHolder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.List;

public class UDPGroupSendThread extends Thread implements BaseUplink {
    private UplinkConfig config;
    private MulticastSocket socket;

    UDPGroupSendThread(UplinkConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        try {
            // 绑定本机地址和随机端口
            InetSocketAddress localAddr = new InetSocketAddress(config.getLocalIp(), 0);
            socket = new MulticastSocket(localAddr);
            System.out.println("创建" + config.getName() + "成功");
        } catch (IOException e) {
                System.err.println("上行: 创建udp组播线程失败：" + config.getName());
                System.err.println("message：" + e.getMessage());
            return;
        }

        for (UplinkDataHolder uplinkDataHolder : config.getData()) {
            if (uplinkDataHolder.getDataType().equalsIgnoreCase(Constants.DATA_UNIT)) {
                // 如果是data unit，则初始化后，放入dataMap
                new DataUnitLooper(this, uplinkDataHolder,config.getName()).start();
            } else if (uplinkDataHolder.getDataType().equalsIgnoreCase(Constants.DATA_QUEUE)) {
                new DataQueueLooper(this, uplinkDataHolder,config.getName()).start();
            }
        }
    }

    @Override
    public void joinChecksAndSendDataUnit(DataUnit unit, List<CheckConfig> checks) {
        try {
            DataUtil.joinChecks(unit, checks);
            DatagramPacket packet = new DatagramPacket(unit.bytes, unit.bytes.length, InetAddress.getByName(config.getAddr()), config.getPort());
            socket.send(packet);
            // 发送数据后，增加该socket的帧发送数
            GlobalDataHolder.increaseCount(config.getName());
        } catch (IOException e) {
            if(Global_SocketLib.runMode == 1) {
                System.err.println("udp组播发送数据失败: " + config.getName());
                System.err.println("message：" + e.getMessage());
            }
        }

    }

    @Override
    public void joinChecksAndSendDataUnit(DataUnit unit, UplinkDataHolder dataHolder) {
        try {
            DataUtil.joinChecks(unit, dataHolder.getChecks());
            DatagramPacket packet = new DatagramPacket(unit.bytes, unit.bytes.length, InetAddress.getByName(config.getAddr()), config.getPort());
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
                socket.send(packet);
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
            socket.send(packet);
            // 发送数据后，增加该socket的帧发送数
            GlobalDataHolder.increaseCount(config.getName());
        } catch (IOException e) {
            System.err.println("udp组播发送数据失败: " + config.getName());
            System.err.println("message：" + e.getMessage());
        }

    }

}