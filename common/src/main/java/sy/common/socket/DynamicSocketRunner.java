package sy.common.socket;

import sy.common.socket.downlink.DownlinkReceiver;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.DownlinkConfig;
import sy.common.socket.entity.DownlinkDataHolder;
import sy.common.socket.global.DataUtil;
import sy.common.socket.global.GlobalDataHolder;
import sy.common.socket.global.SocketConfig;
import sy.common.socket.notifier.DataNotifier;
import sy.common.socket.notifier.SocketStatusNotifier;
import sy.common.socket.uplink.UplinkSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DynamicSocketRunner {
    private static final Logger LOGGER_MAIN = LoggerFactory.getLogger(DynamicSocketRunner.class);

    public static SocketConfig config;
    public static UplinkSender uplinkSender;
    public static DownlinkReceiver downlinkReceiver;
    public static Map<String,ILoopThreadControl> loopThreadMap = new HashMap<>();

    public static DownlinkDataHolder getDownlinkDataHolder(String key){
        for(DownlinkConfig downlinkConfig : config.downlinks){
            for(DownlinkDataHolder holder: downlinkConfig.getData()){
                if(key == holder.getKey())
                    return holder;
            }
        }
        return null;
    }

    /**
     * 启动方法，默认使用resources下的config.yml文件
     */
    public static boolean start(DataNotifier dataNotifier, SocketStatusNotifier statusNotifier) {
        GlobalDataHolder.dataNotifier = dataNotifier;
        GlobalDataHolder.socketStatusNotifier = statusNotifier;
        Yaml yaml = new Yaml();
        config = yaml.loadAs(DynamicSocketRunner.class.getClassLoader().getResourceAsStream("socketConfig.yml"), SocketConfig.class);
        initData(config);
        uplinkSender = new UplinkSender(config.getUplinks());
        downlinkReceiver = new DownlinkReceiver(config.getDownlinks());
        return true;
    }

    /**
     * 启动方法
     *
     * @param dataNotifier   datanotifier
     * @param statusNotifier socketstatusNotifier
     * @param configPath     配置文件路径
     */
    public static boolean start(DataNotifier dataNotifier, SocketStatusNotifier statusNotifier, String configPath) {
        GlobalDataHolder.dataNotifier = dataNotifier;
        GlobalDataHolder.socketStatusNotifier = statusNotifier;

//        SocketConfig config;
        try {
            config = readConfig(configPath);
        } catch (FileNotFoundException e) {
            System.err.println("配置文件路径错误");
            return false;
        }
        initData(config);
        if(config.getUplinks() != null && config.getUplinks().size() > 0 ) {
            uplinkSender = new UplinkSender(config.getUplinks());
        }else{
            LOGGER_MAIN.warn("未配置发送接口（可选项）！");
        }
        if(config.getDownlinks() != null && config.getDownlinks().size() > 0){
            downlinkReceiver = new DownlinkReceiver(config.getDownlinks());
        }else{
            LOGGER_MAIN.warn("未配置接收接口（可选项）！");
        }
        return true;
    }

    private static SocketConfig readConfig(String path) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        return yaml.loadAs(new FileReader(path), SocketConfig.class);
    }

    private static void initData(SocketConfig config) {
        //queues和dataUnits可以不配置
        if(config.dataUnits != null && config.dataUnits.size() > 0 ) {
            config.dataUnits.forEach(i -> {
                String key = i.getKey();
                DataUnit unit = DataUtil.assembleDataUnit(i);
                GlobalDataHolder.dataMap.put(key, unit);
            });
        }else{
            LOGGER_MAIN.warn("未配置初始dataUnits容器（可选项）！");
            return;
        }
        if(config.queues != null && config.queues.size() > 0 ) {
            config.queues.forEach(i ->
                    GlobalDataHolder.queueMap.put(i, new LinkedList<>()));
        }else{
            LOGGER_MAIN.warn("未配置初始queues容器（可选项）！");
        }
    }

    public static void closeAllThrd(){
        //关闭所有socket线程
        for(Map.Entry<String,ILoopThreadControl> entry: loopThreadMap.entrySet()){
            entry.getValue().stopRunning();
        }
    }
}
