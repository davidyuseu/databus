package sy.databus.view.watch;


import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import sy.databus.process.analyse.ParamsUnion_CNTXT;

/**
 * processor: {@link ParamsUnion_CNTXT}
 * */
@WatchPaneConfig(initialHeight = 70.0, initialWidth = 118.0)
public class ParamsUnionWatchPane extends WatchPane {

    private Label sticker = new Label("\uF076");

    public ParamsUnionWatchPane() {
        this.getStyleClass().add("paramsCollectorWP");
        headerBox.getStyleClass().add("collectorHeader");
        sticker.getStyleClass().add("sticker");
        sticker.setFont(Font.font("FontAwesome"));
        addToContent(sticker);
        contentLayout.setAlignment(Pos.CENTER);
    }

    @Override
    public void refresh() {

    }

    @Override
    protected void dissociate() {

    }

    @Override
    public void stopForEditMode() {

    }
}
