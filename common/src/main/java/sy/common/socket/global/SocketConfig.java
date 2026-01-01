package sy.common.socket.global;

import sy.common.socket.entity.DownlinkConfig;
import sy.common.socket.entity.UplinkConfig;
import sy.common.socket.entity.UplinkDataHolder;
import lombok.Data;
import sy.common.socket.entity.DownlinkConfig;
import sy.common.socket.entity.UplinkConfig;
import sy.common.socket.entity.UplinkDataHolder;

import java.util.List;

@Data
public class SocketConfig {
    public List<UplinkConfig> uplinks;//上行socket配置对象列表
    public List<DownlinkConfig> downlinks;
    public List<String> queues;
    public List<UplinkDataHolder> dataUnits;

    public UplinkConfig getUplinkCfgByName(String name){
        for(UplinkConfig ulc:uplinks){
            if(ulc.getName()==name){
                return ulc;
            }
        }
        return null;
    }

    public DownlinkConfig getDownlinkCfgByName(String name){
        for(DownlinkConfig dlc:downlinks){
            if(dlc.getName()==name){
                return dlc;
            }
        }
        return null;
    }
}
