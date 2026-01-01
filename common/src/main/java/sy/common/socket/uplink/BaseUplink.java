package sy.common.socket.uplink;

import sy.common.socket.entity.CheckConfig;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.UplinkDataHolder;
import sy.common.socket.entity.CheckConfig;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.UplinkDataHolder;

import java.util.List;

public interface BaseUplink {
    /**
     * 加入校验并且发送数据
     *
     * @param dataUnit
     * @param checks
     */
    void joinChecksAndSendDataUnit(DataUnit dataUnit, List<CheckConfig> checks);
    void joinChecksAndSendDataUnit(DataUnit dataUnit, UplinkDataHolder dataHolder);
}
