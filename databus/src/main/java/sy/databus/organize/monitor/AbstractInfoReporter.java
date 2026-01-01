package sy.databus.organize.monitor;

import lombok.Getter;
import sy.databus.entity.property.SimpleStrProperty;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.view.monitor.FrameProcTab;
import sy.databus.view.monitor.InDevTab;

/**
 * {@link MonitorGroup#FRAME_PROC} => {@link DefaultMsgProcessorInfoReporter} => {@link FrameProcTab}
 * {@link MonitorGroup#IN_DEV} => {@link DevInfoReporter} => {@link InDevTab}
 *
 * */
public abstract class AbstractInfoReporter {
    @Getter
    protected final AbstractIntegratedProcessor reportedProcessor;

    @Getter
    protected final SimpleStrProperty itemName;

    @Getter
    protected MonitorGroup group;

    public AbstractInfoReporter(AbstractIntegratedProcessor reportedProcessor, MonitorGroup group) {
        this.reportedProcessor = reportedProcessor;
        this.itemName = reportedProcessor.getName();
        this.group = group;
    }

    //
    abstract public void updateInfo();

    abstract public void resetInfo();

}
