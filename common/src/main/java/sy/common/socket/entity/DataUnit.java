package sy.common.socket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataUnit {
    public byte[] bytes;
    public Map<String,String> map_ParamsContainer = new HashMap<>();
    public Integer count ;//该DU发送次数
    public Long interval ;//该DU重复发送时每次的延时
}
