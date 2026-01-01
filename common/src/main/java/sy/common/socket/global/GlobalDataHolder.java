package sy.common.socket.global;


import lombok.Data;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.ParamsMapInfo;
import sy.common.socket.notifier.DataNotifier;
import sy.common.socket.notifier.SocketStatusNotifier;

import java.util.*;

public class GlobalDataHolder {
    public static Map<String, DataUnit> dataMap = new HashMap<>();              //全局存放DataUnit，包括上行，下行以及初始化DataUnit
    public static Map<String, Queue<DataUnit>> queueMap = new HashMap<>();      //全局存放DataQueue
    public static DataNotifier dataNotifier = null;                             //数据收的监听接口
    public static SocketStatusNotifier socketStatusNotifier = null;             //Socket状体机接口，收发数据时和通断状态发生改变时触发其接口方法
    private static Map<String, Integer> countMap = new HashMap<>();             //Socket收发帧计数状态机
    private static Map<String, SocketStatus> statusMap = new HashMap<>();       //Socket通断状态机
    private static Map<String, TimerTask> taskMap = new HashMap<>();            //状态机任务对象
    private static Timer timer = new Timer();                                   //状态机定时器

    public static Map<String, ParamsMapInfo> paramsMaps = new HashMap<>();      //全局存放解参表

    public static Map<String, TimerTask> getTaskMap() {
        return taskMap;
    }

    public static void setTaskMap(Map<String, TimerTask> taskMap) {
        GlobalDataHolder.taskMap = taskMap;
    }

    public synchronized static void increaseCount(String key) {
        if (countMap.containsKey(key))
            countMap.put(key, countMap.get(key) + 1);
        else
            countMap.put(key, 1);
        socketStatusNotifier.onDataCountUpdate(key, countMap.get(key));
    }

    public synchronized static void setAliveAndReCountDown(String key, long countDown) {
        SocketStatus curr = statusMap.get(key);
        if (!curr.isAlive()) {
            curr.setAlive(true);
            socketStatusNotifier.onSocketStatusUpdate(key, true);
        }
        // 把之前的定时任务取消
        taskMap.get(key).cancel();
        taskMap.remove(key);
        initCountdown(key, countDown);
    }

    public synchronized static boolean allStateBreak(){
        if(statusMap == null || statusMap.size() <= 0)
            return true;
        for(Map.Entry<String, SocketStatus> entry : statusMap.entrySet()){
            if(entry.getValue().isAlive())
                return false; //只要有一个活的状态机就返回true
        }
        return true;
    }

    /**
     * 初始化倒计时，倒计时结束后将该socket状态设置为false
     *
     * @param delay 延时时间，以毫秒为单位
     */
    public synchronized static void initCountdown(String key, long delay) {
        // 先记录初始化任务的时间
        SocketStatus status;
        // 初始化定时器时，socket状态为连通状态
        if (!statusMap.containsKey(key)) {
            status = new SocketStatus();
            status.setAlive(true);
            socketStatusNotifier.onSocketStatusUpdate(key, true);
        } else {
            status = statusMap.get(key);
        }
        statusMap.put(key, status);
        // 开始任务
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                SocketStatus curr = statusMap.get(key);
                curr.setAlive(false);
                socketStatusNotifier.onSocketStatusUpdate(key, false);
            }
        };
        taskMap.put(key, task);
        timer.schedule(task, delay);
    }

    @Data
    static class SocketStatus {
        boolean alive;
        long lastUpdate;
    }

    public synchronized static void closeAllTask(){
        //关闭所有状态机任务
        for(Map.Entry<String, TimerTask> entry: taskMap.entrySet()){
            entry.getValue().cancel();
        }
        timer.cancel();//必须把定时器关闭
    }
}
