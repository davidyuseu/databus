package sy.common.socket.entity;

import javafx.beans.property.SimpleIntegerProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


public class ParamsMapInfo {
    //解参表刷新次数（可观察，用于发送解参表刷新通知）
    @Getter @Setter
    private SimpleIntegerProperty paramsMapRefreshCount = new SimpleIntegerProperty(0);
    //解参表
    @Getter @Setter
    private Map<String,String> paramsMap = new HashMap<>();
}
