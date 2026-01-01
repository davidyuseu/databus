package sy.databus.organize.monitor;

import javafx.scene.layout.FlowPane;
import lombok.Getter;
import lombok.Setter;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.view.customskins.titled.TitledNodeSkin;

public class WatchPaneShifter extends AbstractInfoReporter {

    @Getter @Setter
    private TitledNodeSkin titledNodeSkin = null;

    @Getter @Setter
    private FlowPane shifter = null;

    public WatchPaneShifter(AbstractIntegratedProcessor processor) {
        super(processor, MonitorGroup.CONTROL);
    }

    @Override
    public void updateInfo() {
        // empty impl or refresh the watchPane
    }

    @Override
    public void resetInfo() {
        // empty impl or reset the watchPane
    }
}
