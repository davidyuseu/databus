package sy.common.socket.entity;

import lombok.Data;

@Data
class BaseSocketConfig {
    private String name, addr, socketType, localIp;
    private int port;
    private Long aliveTime = 2000L;//默认存活时间判断2秒
    private long lastDataTime;//该socket上一次接收或发送时的时间记录
}
