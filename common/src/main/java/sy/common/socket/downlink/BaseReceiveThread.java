package sy.common.socket.downlink;

import sy.common.socket.Global_SocketLib;
import sy.common.socket.entity.*;
import sy.common.socket.global.Constants;
import sy.common.socket.global.DataUtil;
import sy.common.socket.global.GlobalDataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sy.common.socket.entity.DownlinkConfig;
import sy.common.socket.entity.DownlinkDataHolder;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 父类线程，共享checkAndSaveData方法
 */
class BaseReceiveThread extends Thread {
    private static final Logger LOGGER_MAIN = LoggerFactory.getLogger(BaseReceiveThread.class);
    /**
     * 接收到数据时，进行验证和保存
     * @param data0
     * @param config
     */
    protected void checkAndSaveData(byte[] data0, DownlinkConfig config, int recLen) {
        byte[] data = new byte[recLen];
        System.arraycopy(data0,0,data,0,recLen);

        for (DownlinkDataHolder holder : config.getData()) {
            //判延时
            if(holder.getInterval()!=null && holder.getInterval()>0){//如果将延迟不设置或设为0，则不比较上一次时间，即刻处理
                long crnTime = System.currentTimeMillis();
                if (crnTime - holder.getLastCheckTime() < holder.getInterval()) {
                    continue;//如果没达到设置的延迟就不处理该data
                } else {
                    holder.setLastCheckTime(crnTime);
                }
            }
            //判帧长
            if(holder.getLength()==null){
                LOGGER_MAIN.info("{}","未配置帧长");
            }else{
                if(holder.getLength()!=recLen){
                    LOGGER_MAIN.error("{} 校验帧长失败！", holder.getKey());
                    continue;
                }else{
                    LOGGER_MAIN.debug("{}", "校验帧长成功!");
                }
            }

            String str = "";
            boolean pass = true;
            // 检查数据头部是否一致
            if (holder.getHead()==null)
            {
                if(Global_SocketLib.runMode == 1)
                    System.out.println("未配置帧头");
            }else {
                str = DataUtil.replaceBlank(holder.getHead());
                for (int i = 0; i < str.length(); i += 2) {
                    byte value = (byte) Integer.parseInt(str.substring(i, i + 2), 16);
                    if (value != data[i / 2]) {
                        pass = false;
                        break;
                    }
                }
                if (!pass) {
                    System.err.println(holder.getKey() + "校验head失败\n");
                    continue;
                }
                if(Global_SocketLib.runMode == 1)
                    System.out.println("校验head成功");
            }
            // 检查标识位
            // mod by lyx
            if (holder.getIdentities()==null)
            {
                if(Global_SocketLib.runMode == 1)
                    System.out.println("未配置标识位");
            }else {
                for (Identity identity : holder.getIdentities()) {
                    str = DataUtil.replaceBlank(identity.getValue());
                    for (int i = 0; i < str.length(); i += 2) {
                        byte value = (byte) Integer.parseInt(str.substring(i, i + 2), 16);
                        if (value != data[identity.getStartPos() + i / 2]) {
                            pass = false;
                            break;
                        }
                    }
                    if (!pass)
                        break;
                }
                if (!pass) {
                    if(Global_SocketLib.runMode == 1)
                        System.err.println(holder.getKey() + "标识位匹配失败\n");
                    continue;
                }
            }
            if (holder.getFrameTypes()==null)
            {
                if(Global_SocketLib.runMode == 1)
                    System.out.println("未配置帧类型项");
            }else {
                for (FrameType frameType : holder.getFrameTypes()) {
                    str = DataUtil.replaceBlank(frameType.getNewByteValue());
                    int bitStart = frameType.getBitStart();
                    int bitEnd = frameType.getBitEnd();
                    byte newByte = data[frameType.getBytePos()];
                    int bit = (int)((newByte>>bitStart)&(0xFF>>(8-(bitEnd-bitStart+1))));
                    newByte = (byte)bit;
                    byte myNewByte = (byte) Integer.parseInt(str, 16);
                    if (myNewByte != newByte ) {
                        pass = false;
                        break;
                    }
                }
                if (!pass) {
                    if(Global_SocketLib.runMode == 1)
                        System.err.println(holder.getKey() + "帧类型匹配失败\n");
                    continue;
                }
            }
            if(Global_SocketLib.runMode == 1)
                System.out.println("开始进行check校验");
            if (holder.getChecks()==null)
            {
                if(Global_SocketLib.runMode == 1)
                    System.out.println("未配置check项");
            }else if (!DataUtil.checkData(data, holder.getChecks())) {
                if(Global_SocketLib.runMode == 1)
                    System.err.println(holder.getKey() +"check校验失败\n");
                continue;
            }

            DataUnit dataUnit = new DataUnit();
            dataUnit.bytes = data;

            if(Global_SocketLib.runMode == 1)
                System.out.println("校验通过\n");
            if (holder.getDataType().equalsIgnoreCase(Constants.DATA_UNIT)) {
                GlobalDataHolder.dataMap.put(holder.getKey(), dataUnit);
                //存入本次校验通过的时间点
                holder.setLastValidRecvTime(System.currentTimeMillis());
                //帧校验通过，增加Data项帧计数
                GlobalDataHolder.increaseCount(holder.getKey());
                //帧校验通过，将Data项状态设置为alive=true，并且重新进行倒计时
                if(holder.getAliveTime()!=null)
                    GlobalDataHolder.setAliveAndReCountDown(holder.getKey(), holder.getAliveTime());
                //最后触发通知接口
                GlobalDataHolder.dataNotifier.onDataUnitUpdate(holder.getKey(), dataUnit);
            } else {
                // TODO 这里put到queueMap中了吗
                Queue<DataUnit> queue = GlobalDataHolder.queueMap.computeIfAbsent(holder.getKey(), k -> new LinkedList<>());
                queue.add(dataUnit);
                GlobalDataHolder.dataNotifier.onQueueUpdate(holder.getKey(), queue);
            }
        }
    }
}
