package sy.databus;

import javafx.application.Platform;
import lombok.Setter;
import sy.databus.view.customskins.titled.TitledNodeSkin;

public class WatchersRefreshTask extends UITask {

    @Setter
    private volatile boolean monitorActive = false;

    protected WatchersRefreshTask() {
        task = () -> {
            var nodeSkins = MainPaneController.getNodeSkins();
            if (nodeSkins != null && !nodeSkins.isEmpty()) {
                Platform.runLater(() -> {
                    for (var entry : nodeSkins.entrySet()) {
                        if (entry.getValue() instanceof TitledNodeSkin titledNodeSkin) {
                            titledNodeSkin.getContentRoot().refresh();
                        }
                    }
                });
            }
            if (monitorActive) {
                Platform.runLater(() -> {
                    MainPaneController.INSTANCE.getMonitorsManager()
                            .refreshAllMonitorTabs();
                });
            }
        };
    }
}
