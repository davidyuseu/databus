package sy.databus.view.watch;

import javafx.application.Platform;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.dev.RealTimeTaskLauncher;

/**
 * processor: {@link RealTimeTaskLauncher}
 * */
@WatchPaneConfig(initialHeight = 70.0, initialWidth = 118.0)
public class RealTimeTaskLauncherWatchPane extends BaseBootWatchPane {

    private RealTimeTaskLauncher boot;

    public RealTimeTaskLauncherWatchPane() {
        btnBoot.setOnAction(event -> {
            btnBoot.setDisable(true);
            btnEnd.setDisable(false);
            this.boot.fireParsingStart((ob, oldVar, newVar) -> {
                if (newVar) {
                    this.complete();
                }
            });
        });

        btnEnd.setOnAction(event -> {
            btnEnd.setDisable(true);
            this.boot.fireParsingEnd();
        });
    }

    @Override
    public void associateWith(AbstractIntegratedProcessor processor) {
        super.associateWith(processor);
        boot = (RealTimeTaskLauncher) processor;
    }

    @Override
    public void refresh() {}

    @Override
    protected void dissociate() {}

    private void complete() {
        Platform.runLater(() -> {
            btnBoot.setDisable(false);
            btnEnd.setDisable(true);
        });
    }
}
