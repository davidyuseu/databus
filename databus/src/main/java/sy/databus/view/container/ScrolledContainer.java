package sy.databus.view.container;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import sy.common.cache.ICache;
import sy.common.util.SStringUtil;

import static sy.databus.view.container.Constants.STYLE_CLASS_CACHED_PANE;

public class ScrolledContainer extends TitledPane implements ICache, IContainer {
    public static final String STYLE_CLASS_OUTER_SCROLLED ="outer-scrolled-titled";

    public ScrolledContainer(String title) {
        this.getStyleClass().add(STYLE_CLASS_CACHED_PANE);

        VBox content = new VBox();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(content);
        this.setContent(scrollPane);
        this.setText(title);
        this.setId(SStringUtil.removeBlank(title));

        this.getStyleClass().add(STYLE_CLASS_OUTER_SCROLLED);
    }

    @Override
    public void rebuild(String title) {
        this.setText(title);
        this.setId(SStringUtil.removeBlank(title));
    }

    @Override
    public void clean() {
        ScrollPane scrollPane = (ScrollPane) this.getContent();
        VBox content = (VBox) scrollPane.getContent();
        content.getChildren().removeAll(content.getChildren());
    }
}
