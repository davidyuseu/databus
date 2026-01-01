package sy.common.socket.entity;

import sy.common.socket.global.GlobalDataHolder;
import javafx.beans.property.SimpleIntegerProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从downlink的socket接收到数据的持有类
 */
@Data
public class DownlinkDataHolder {//对应的yml的data配置项
    private String key="";
    private Integer length;
    // 每隔多少毫秒接收一次
    private Long interval;
    //该Data项数据存活时间判断
    private Long aliveTime;
    //该Data项上一次接收到数据并验证通过的时间点（在yml中不配置）
    private long lastValidRecvTime;
    private String dataType ="dataunit";
    private String head ="";
    //validate data pos
    private int validateDataPos = 0;
    private List<Identity> identities;
    private List<CheckConfig> checks;
    private List<FrameType> frameTypes;

    private String numProtocolFile="";
    private String charProtocolFile="";

    public void setNumProtocolFile(String numProtocolFile) {
        this.numProtocolFile = numProtocolFile;
        if(!numProtocolFile.equals("")){
            GlobalDataHolder.paramsMaps.put(this.getKey(),new ParamsMapInfo());
        }
    }

    public void setCharProtocolFile(String charProtocolFile) {
        this.charProtocolFile = charProtocolFile;
        if(!numProtocolFile.equals("")){
            GlobalDataHolder.paramsMaps.put(this.getKey(),new ParamsMapInfo());
        }
    }

    //上一次该帧data参与校验的时间
    private Long lastCheckTime = -10000L;
    //解参表刷新次数（可观察，用于发送解参表刷新通知）
    private SimpleIntegerProperty paramsMapRefreshCount = new SimpleIntegerProperty(0);
    //解参表
    private Map<String,String> paramsMap = new HashMap<>();
    //解参类
//    private CharNumParamParser tmdRe = new CharNumParamParser();

}