package sy.databus.organize.monitor;

import lombok.Getter;

public enum MonitorGroup {
    CONTROL("监控组件"),
    IN_DEV("输入设备"),
    OUT_DEV("输出设备"),
    FRAME_PROC("帧处理");

    @Getter
    String display;

    MonitorGroup(String display) {
        this.display = display;
    }
}
