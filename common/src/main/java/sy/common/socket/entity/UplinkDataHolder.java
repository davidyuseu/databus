package sy.common.socket.entity;

import lombok.Data;

import java.util.List;

/**
 * 从uplink的socket接收到数据的持有类
 */
@Data
public class UplinkDataHolder {//上行socket配置对象中的帧配置
    private String key;
    private Long interval;
    private String head;
    private Integer count;
    private Integer length;
    private String dataType;
    private List<Identity> identities;//该帧对象下的标识位配置列表(identities:)
    private List<CheckConfig> checks;
}