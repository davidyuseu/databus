package sy.common.socket.downlink;

import sy.common.socket.entity.DownlinkConfig;
import sy.common.socket.global.Constants;
import lombok.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 下行接收
 */
@Data
public class DownlinkReceiver {
    private Map<String,Thread> threadMap = new Hashtable<>();

    public DownlinkReceiver(List<DownlinkConfig> downlinks) {
        for (DownlinkConfig config : downlinks) {

            if (config.getSocketType() == null) {
                System.err.println("请指定" + config.getName() + "的socket类型!");
            }
            if (config.getSocketType().equalsIgnoreCase(Constants.UDP_GROUP)) {//组播接收
                Thread thread = new UDPGroupReceiveThread(config);
                thread.start();
                threadMap.put(config.getName(),thread);
            } else if (config.getSocketType().equalsIgnoreCase(Constants.TCP_SERVER)) {//tcp服务端接收
                Thread thread = new TCPServerReceiveThread(config);
                thread.start();
                threadMap.put(config.getName(),thread);
            } else if (config.getSocketType().equalsIgnoreCase(Constants.TCP_CLIENT)) {//tcp客户端接收
                try {
                    InetAddress address = InetAddress.getByName(config.getLocalIp());
                    // 绑定到本机地址（区分多网卡），使用随机端口
                    Socket socket = new Socket(config.getAddr(), config.getPort(), address, 0);
                    System.out.println("创建tcp client " + config.getName() + "成功");
                    Thread thread = new TCPReceiveThread(config, socket, null);
                    thread.start();
                    threadMap.put(config.getName(),thread);
                } catch (IOException e) {
                    System.err.println("下行：tcp client: " + config.getName() + "连接失败:" + config.getAddr());
                }
            } else {
                System.err.println(config.getName() + " 未知socket类型:" + config.getSocketType());
            }
        }
    }
}
