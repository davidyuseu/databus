package sy.databus.view.container;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import sy.common.cache.ICache;

import static sy.databus.view.container.Constants.STYLE_CLASS_CACHED_PANE;
import static sy.databus.view.container.Constants.STYLE_CLASS_UNTITLED_CONTENT;

public class ScrolledContent extends ScrollPane implements ICache {
    public static final String STYLE_CLASS_SCROLLED_CONTENT = "scrolled-content";

    public ScrolledContent(){
        this.getStyleClass().add(STYLE_CLASS_CACHED_PANE);

        VBox content = new VBox();
        content.getStyleClass().add(STYLE_CLASS_UNTITLED_CONTENT);
        this.setContent(content);
    }

    @Override
    public void clean() {
        VBox content = (VBox) this.getContent();
        content.getChildren().removeAll(content.getChildren());
    }
}
