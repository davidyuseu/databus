package sy.databus.view;

import javafx.scene.control.Label;

import static sy.databus.global.Constants.COLOR_ITEM_NAME;

public class SLabel extends Label {

    public SLabel() {
        this.getStyleClass().add("controllerStyle0");
    }

    public SLabel(String text) {
        super(text);
        this.getStyleClass().add("controllerStyle0");
    }

    private static final String DEFAULT_STYLE_ITEM_NAME = "-fx-text-fill: " + COLOR_ITEM_NAME;

    public void setNormalDisplay() {
        this.setStyle(DEFAULT_STYLE_ITEM_NAME);
    }

    public void setErrorDisplay() {
        this.setStyle("-fx-text-fill: #FF0000FF");
    }

    public void setWarnDisplay() {
        this.setStyle("-fx-text-fill: #FF7700FF");
    }
}
