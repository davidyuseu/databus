package sy.databus.view.container;

import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import sy.common.cache.ICache;
import sy.common.util.SStringUtil;

import static sy.databus.view.container.Constants.STYLE_CLASS_CACHED_PANE;

public abstract class Container extends TitledPane implements ICache, IContainer {
    public Container(String title) {
        this.getStyleClass().add(STYLE_CLASS_CACHED_PANE);

        VBox content = new VBox();
        this.setContent(content);
        this.setText(title);
        this.setId(SStringUtil.removeBlank(title));
    }

    @Override
    public void rebuild(String title) {
        this.setText(title);
        this.setId(SStringUtil.removeBlank(title));
    }

    @Override
    public void clean() {
        VBox content = (VBox) this.getContent();
        content.getChildren().removeAll(content.getChildren());
    }
}