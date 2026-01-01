package sy.databus.view.watch;

import javafx.application.Platform;
import sy.databus.process.AbstractIntegratedProcessor;

/**
 * processor: {@link DataRecordLauncher}
 * */
@WatchPaneConfig(initialHeight = 70.0, initialWidth = 118.0)
public class DataRecordLauncherWatchPane extends BaseBootWatchPane {

    private DataRecordLauncher boot;

    public DataRecordLauncherWatchPane() {
        btnBoot.setOnAction(event -> {
            if (this.boot.fireStartSig((ob, oldVar, newVar) -> {
                if (newVar) {
                    this.complete();
                }
            })) {
                btnBoot.setDisable(true);
                btnEnd.setDisable(false);
            }
        });

        btnEnd.setOnAction(event -> {
            if (this.boot.fireEndSig())
                btnEnd.setDisable(true);
        });
    }

    @Override
    public void associateWith(AbstractIntegratedProcessor processor) {
        super.associateWith(processor);
        boot = (DataRecordLauncher) processor;
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
