package sy.common.socket.downlink;

import sy.common.socket.DynamicSocketRunner;
import sy.common.socket.ILoopThreadControl;
import sy.common.socket.entity.DownlinkConfig;
import sy.common.socket.global.Constants;
import sy.common.socket.global.GlobalDataHolder;
import sy.common.socket.DynamicSocketRunner;
import sy.common.socket.ILoopThreadControl;
import sy.common.socket.entity.DownlinkConfig;
import sy.common.socket.global.Constants;
import sy.common.socket.global.GlobalDataHolder;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;

public class TCPReceiveThread extends BaseReceiveThread implements ILoopThreadControl {
    DownlinkConfig config;
    Socket socket;
    TCPServerReceiveThread serverThread;

    public TCPReceiveThread(DownlinkConfig config, Socket socket, TCPServerReceiveThread serverThread) {
        this.config = config;
        this.socket = socket;
        this.serverThread = serverThread;
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
        //running.set(true);
        try {
            while (running) {
                //# 接收数据包缓冲区预设大小，并非实际待校验帧长，如果是-1，则预设缓冲区长度为4096
                int len = config.getLength();
                if (len == -1)
                    len = Constants.DEFAULT_LEN;
                byte[] bytes = new byte[len];
                // server tcp时，倒计时在server线程里设置
                // 只有tcp client时，倒计时才在这里设置
                if (serverThread == null) {
                    GlobalDataHolder.initCountdown(config.getName(), config.getAliveTime());
                }
                InputStream in = socket.getInputStream();
                int recLen = in.read(bytes);
                if (recLen > 0) {
                    GlobalDataHolder.increaseCount(config.getName());
                    GlobalDataHolder.setAliveAndReCountDown(config.getName(), config.getAliveTime());
                    System.out.println(config.getName() + "接收到ip为：" + socket.getRemoteSocketAddress());
                    System.out.println(config.getName() + ":" + Arrays.toString(bytes));
                    System.out.println(config.getName() + ":" + new String(bytes));
                    System.out.println("开始校验");
                    checkAndSaveData(bytes, config, recLen);
                }
            }
        } catch (IOException e) {
            System.err.println("下行：tcp 读取数据失败: " + config.getName());
            System.err.println("message: " + e.getMessage());
        }
    }

}
