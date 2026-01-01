package sy.databus.view.watch;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import sy.databus.view.customskins.titled.TitledNodeSkin;

import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

public abstract class FoldingWatchPane extends WatchPane {

    protected SimpleBooleanProperty expandedProperty = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            super.invalidated();
            if (get()) { // 展开
                rootSkin.getRoot().resize(rootSkin.getItem().getWidth(), rootSkin.getItem().getHeight());
                contentLayout.setVisible(true);
                contentLayout.setManaged(true);
                rootSkin.getRoot().setDraggedResizable(true);
            } else { // 折叠
                rootSkin.getRoot().setDraggedResizable(false);
                contentLayout.setVisible(false);
                contentLayout.setManaged(false);
                rootSkin.getRoot().resize(rootSkin.getItem().getWidth(), TitledNodeSkin.TITLED_FOLD_HEIGHT);
            }
        }
    };

    public FoldingWatchPane() {
        headerBox.addEventHandler(MOUSE_RELEASED, headerMouseEventHandler);

        contentLayout.setVisible(false);
        contentLayout.setManaged(false);
    }

    public void setExpanded(boolean flag) {
        expandedProperty.set(flag);
    }

    // 防止出现如下情况：
    // 移动titleNode之后，松开鼠标，又对其进行展开。
    private EventHandler<MouseEvent> headerMouseEventHandler = mouseEvent -> {
        if (!mouseEvent.isDragDetect()) {
            Event.fireEvent(rootSkin.getRoot(), mouseEvent);
            mouseEvent.consume();
        } else {
            expandedProperty.set(!expandedProperty.get());
        }
    };

    @Override
    protected void dissociate() {
        // 删除前必须折叠，否则回退时会出现显示bug
        this.setExpanded(false);
    }
}
