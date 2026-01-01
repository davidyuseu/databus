package sy.databus.view.container;

import javafx.scene.layout.VBox;
import sy.common.cache.ICache;

import static sy.databus.view.container.Constants.STYLE_CLASS_CACHED_PANE;

public class Content extends VBox implements ICache {

    public Content() {
        this.getStyleClass().add(STYLE_CLASS_CACHED_PANE);

        this.getStyleClass().add(Constants.STYLE_CLASS_UNTITLED_CONTENT);
    }

    @Override
    public void clean() {
        this.getChildren().removeAll(this.getChildren());
    }
}
