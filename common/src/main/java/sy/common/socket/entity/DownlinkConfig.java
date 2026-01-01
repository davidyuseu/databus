package sy.common.socket.entity;

import lombok.Data;

import java.util.List;

@Data
public class DownlinkConfig extends BaseSocketConfig {
    private int length;//接收帧缓冲区预设大小，并非实际接收帧校验大小
    private List<DownlinkDataHolder> data;

    public DownlinkDataHolder getDownLkDataHolderByKey(String key){
        for(DownlinkDataHolder dldh:data){
            if(dldh.getKey()==key){
                return dldh;
            }
        }
        return null;
    }
}
