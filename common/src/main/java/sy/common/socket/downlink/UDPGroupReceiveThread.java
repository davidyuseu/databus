package sy.common.socket.downlink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sy.common.socket.DynamicSocketRunner;
import sy.common.socket.ILoopThreadControl;
import sy.common.socket.entity.DownlinkConfig;
import sy.common.socket.entity.DownlinkDataHolder;
import sy.common.socket.global.Constants;
import sy.common.socket.global.GlobalDataHolder;
import sy.common.util.SComponentUtils;

import java.io.IOException;
import java.net.*;
import java.util.Objects;

public class UDPGroupReceiveThread extends BaseReceiveThread implements ILoopThreadControl {
    private static final Logger LOGGER_MAIN = LoggerFactory.getLogger(UDPGroupReceiveThread.class);

    private DownlinkConfig config;

    UDPGroupReceiveThread(DownlinkConfig config) {
        this.config = config;
        DynamicSocketRunner.loopThreadMap.put(config.getName(),this);
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
        try {
            // 指定本机地址和端口
            InetSocketAddress localAddr = new InetSocketAddress(config.getLocalIp(),config.getPort());
            MulticastSocket socket = new MulticastSocket(localAddr);
            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getByName(config.getLocalIp()));
            Objects.requireNonNull(ni);
            // 加入目的组播(?和发送端口号)，并绑定指定ip的网卡
            socket.joinGroup(new InetSocketAddress(config.getAddr(), config.getPort()), ni);
            LOGGER_MAIN.debug("{}","socket.joinGroup...");
            if(LOGGER_MAIN.isInfoEnabled())
                LOGGER_MAIN.info("创建UDP组播：{} 成功",config.getName());
            //初始化socket的状态机
            if(config.getAliveTime() == null){
                LOGGER_MAIN.debug("{}","未配置Socket存活判断时间，默认为2000ms。");
            }else{
                GlobalDataHolder.initCountdown(config.getName(), config.getAliveTime());
            }
            //初始化该socket下各Data项的状态机
            //读取该socket下各Data项的解参配置文件
            for (DownlinkDataHolder holder : config.getData()) {
                if(holder.getAliveTime() == null){
                    LOGGER_MAIN.debug("{}","未配置Data项存活判断时间。");
                }else{
                    GlobalDataHolder.initCountdown(holder.getKey(), holder.getAliveTime());
                }

                if((!SComponentUtils.replaceBlank(holder.getCharProtocolFile()).equals(""))
                        && holder.getCharProtocolFile() != null) {
                    String filePath = ".\\init\\"+holder.getCharProtocolFile();
//                    holder.getTmdRe().readStrProtocol(new File(filePath));
                }
                if((!SComponentUtils.replaceBlank(holder.getNumProtocolFile()).equals(""))
                        && holder.getNumProtocolFile() != null) {
                    String filePath = ".\\init\\"+holder.getNumProtocolFile();
//                    holder.getTmdRe().readNumProtocol(new File(filePath));
                }
            }
            while (running) {
                //# 接收数据包缓冲区预设大小，并非实际待校验帧长，如果是-1，则预设缓冲区长度为4096
                int len = config.getLength();
                if (len == -1)
                    len = Constants.DEFAULT_LEN;
                byte[] bytes = new byte[len];
                DatagramPacket packet = new DatagramPacket(bytes, len);
                socket.receive(packet);
                int recLen = packet.getLength();
                if(LOGGER_MAIN.isDebugEnabled())
                    LOGGER_MAIN.debug("报文接收，ip为：{}，报文：{}",packet.getAddress().getHostAddress(),packet.getData());
                //socket帧计数
                GlobalDataHolder.increaseCount(config.getName());
                //接收到数据后，将socket状态设置为alive=true，并且重新进行倒计时
                GlobalDataHolder.setAliveAndReCountDown(config.getName(), config.getAliveTime());
                checkAndSaveData(bytes, config, recLen);
            }
        } catch (IOException e) {
            LOGGER_MAIN.error("下行：{} 创建udp组播接收线程失败，异常：{}",config.getName(),e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
