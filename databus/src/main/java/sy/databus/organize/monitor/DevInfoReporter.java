package sy.databus.organize.monitor;

import lombok.Getter;

import sy.databus.process.AbstractMessageProcessor;

public abstract class DevInfoReporter extends DefaultMsgProcessorInfoReporter {

    @Getter
    private String devInfo = "";
    private boolean devInfoCertain = false;

    public DevInfoReporter(AbstractMessageProcessor processor, MonitorGroup group) {
        super(processor, group);
    }

    @Override
    public void updateInfo() {
        super.updateInfo();
        // if tcp, udp, multi-udp
        if (!devInfoCertain) {
            devInfo = updateDevInfo();
        }
        devInfoCertain = true;
    }

    // get the udp/tcp/multi-udp info
    abstract public String updateDevInfo();

    @Override
    public void resetInfo() {
        super.updateInfo();

        devInfoCertain = false;
    }

}
