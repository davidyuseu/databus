package sy.common.socket.uplink;

import sy.common.socket.entity.UplinkConfig;
import sy.common.socket.global.Constants;
import lombok.Data;
import sy.common.socket.entity.UplinkConfig;
import sy.common.socket.global.Constants;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 数据上行线程
 */
@Data
public class UplinkSender {
    private Map<String,Thread> threadMap = new Hashtable<>();

    public UplinkSender(List<UplinkConfig> uplinks) {
        for (UplinkConfig config : uplinks) {
            if (config.getSocketType() == null) {
                System.err.println("请指定" + config.getName() + "的socket类型!");
            }
            // 启动组播
            if (config.getSocketType().equalsIgnoreCase(Constants.UDP_GROUP)) {
                Thread thread = new UDPGroupSendThread(config);
                thread.start();
                threadMap.put(config.getName(),thread);
            } else if (config.getSocketType().equalsIgnoreCase(Constants.TCP)) {
                // 启动tcp发送线程
                Thread thread = new TCPSendThread(config);
                thread.start();
                threadMap.put(config.getName(),thread);
            } else {
                System.err.println(config.getName() + "未知类型:" + config.getSocketType());
            }
        }
    }


}
