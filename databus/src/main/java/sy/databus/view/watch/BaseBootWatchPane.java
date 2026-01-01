package sy.databus.view.watch;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public abstract class BaseBootWatchPane extends WatchPane {
    private static final String BTN_START = "开 始";
    private static final String BTN_END = "结 束";

    protected Button btnBoot = new Button(BTN_START);

    protected Button btnEnd = new Button(BTN_END);

    protected HBox hBox = new HBox(btnBoot, btnEnd);

    public BaseBootWatchPane() {
        this.getStyleClass().add("paramsCollectorWP");
        headerBox.getStyleClass().add("collectorHeader");
        hBox.setSpacing(4d);
        hBox.setAlignment(Pos.CENTER);
        addToContent(hBox);
        contentLayout.setAlignment(Pos.CENTER);
        btnEnd.setDisable(true);
    }

    @Override
    public void stopForEditMode() {
    }
}
