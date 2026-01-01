package sy.common.socket.entity;

import lombok.Data;

import java.util.List;

@Data
public class UplinkConfig extends BaseSocketConfig {//上行socket配置对象
    private List<UplinkDataHolder> data;//上行socket配置对象中的帧配置列表(data:)
}
