package sy.common.socket.downlink;

import sy.common.socket.DynamicSocketRunner;
import sy.common.socket.ILoopThreadControl;
import sy.common.socket.entity.DownlinkConfig;
import sy.common.socket.global.GlobalDataHolder;
import sy.common.socket.DynamicSocketRunner;
import sy.common.socket.ILoopThreadControl;
import sy.common.socket.entity.DownlinkConfig;
import sy.common.socket.global.GlobalDataHolder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerReceiveThread extends Thread implements ILoopThreadControl {
    private DownlinkConfig config;

    TCPServerReceiveThread(DownlinkConfig config) {
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
            InetAddress address = InetAddress.getByName(config.getLocalIp());
            ServerSocket serverSocket = new ServerSocket(config.getPort(), 50, address);
            System.out.println("创建tcp server :" + config.getName() + "成功");
            // 在server端初始化倒计时
            GlobalDataHolder.initCountdown(config.getName(), config.getAliveTime());
            while (running) {
                Socket client = serverSocket.accept();
                System.out.println(config.getName() + "接收到连接:" + client.getRemoteSocketAddress());
                new TCPReceiveThread(config, client, this).start();
            }
        } catch (IOException e) {
            System.err.println("下行：创建tcp server 接收线程失败: " + config.getName());
            System.err.println("message：" + e.getMessage());
        }
    }
}
