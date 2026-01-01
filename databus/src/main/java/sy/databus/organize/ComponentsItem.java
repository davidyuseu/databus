package sy.databus.organize;

import javafx.beans.property.SimpleObjectProperty;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.databus.MainPaneController;
import sy.databus.RightPaneController;
import sy.databus.global.ProcessorType;
import sy.databus.process.AbstractHandler;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.Console;
import lombok.Getter;
import sy.databus.process.frame.handler.Handler;
import sy.databus.view.utils.AwesomeIcon;

import java.util.*;

import static sy.databus.global.Constants.COLOR_ITEM_NAME;
import static sy.databus.global.Constants.COLOR_SEL_ITEM_BACKGROUND;

@Log4j2
public class ComponentsItem<T> extends BaseComponentsItem<T> {

    /** 左边栏组件集合
     *  ps: {@link ProcessorType.Category} 和 {@link Handler.Category} 中不要有同名元素，
     *  否则{@link #visibleComponents}无法区分
     * */
    public static Map<String, Set<ComponentsItem>> visibleComponents = new HashMap<>();

    private static final String mouseHoverStyle = "-fx-background-color: " + COLOR_SEL_ITEM_BACKGROUND;

    private static final String normalStyle = "-fx-background-color: transparent";

    // 全局的当前被选中的Item
    public static SimpleObjectProperty<ComponentsItem> selectedComponent = new SimpleObjectProperty<>(null);

    @Getter
    private Label label = new Label();

    // to add a processor into the graph
    private static Button btnAdd = new Button();

    static {
        selectedComponent.addListener((observableValue, lastedItem, newItem) -> {
            if (lastedItem != null) {
                lastedItem.setStyle(normalStyle);
                lastedItem.getLabel().setTextFill(Paint.valueOf(COLOR_ITEM_NAME));
            }

            if (newItem != null) {
                newItem.getLabel().setTextFill(Paint.valueOf("000000FF"));
            }
        });

        btnAdd.getStyleClass().add("btnAdd");
        btnAdd.setGraphic(AwesomeIcon.PLUS.node());
        btnAdd.setOnAction(e -> {
            addComponent();
        });
    }

    private static void addComponent() {
        ComponentsItem item = selectedComponent.get();
        if (item == null) {
            log.error("{}", "A null component!");
            return;
        }

        Class opClazz = selectedComponent.get().getOpClazz();
        if (AbstractIntegratedProcessor.class.isAssignableFrom(opClazz)) { // 当前选中的是processor
            MainPaneController.INSTANCE.addNode();
        } else if (AbstractHandler.class.isAssignableFrom(opClazz)) { // 当前选中的是handler
            addHandler(opClazz);
        } else {
            log.error("unknown component!");
        }
    }

    @SneakyThrows
    private static void addHandler(Class opClazz){
        TitledPane expandedPane = RightPaneController.INSTANCE.getOuterContainers().getExpandedPane();
        if (expandedPane == null)
            return;
        String containerId = expandedPane.getId();
        Console.Category category = Console.Category.valueOfNonBlankName(containerId);
        HandlersViewContext handlersViewContext = HandlerConsoleCache.handlersViewContextMap.get(category);
        AbstractHandler handler = (AbstractHandler) opClazz.getDeclaredConstructor().newInstance();
        handler.initialize();
        String name = handler.getClass().getAnnotation(Handler.class).name();
        handler.setName(name);
        handlersViewContext.getHandlersView().getSyncObList().add(handler);
    }

    public ComponentsItem(Class<? extends T> opClazz) {
        super(opClazz);

        label.setText(opClazz.getSimpleName());
        label.setTextFill(Paint.valueOf(COLOR_ITEM_NAME));
        HBox.setHgrow(label, Priority.ALWAYS);

        itemPane.getChildren().add(label);

        // 鼠标进入 整体
        this.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEvent -> {
            if (bindingPane != null) {
                bindingPane.setVisible(true);
                bindingPane.setManaged(true);
            }
        });

        // 鼠标离开 整体
        this.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> {
            if (bindingPane != null) {
                bindingPane.setVisible(false);
                bindingPane.setManaged(false);
            }
        });

        // 鼠标进入 主itemPane
        itemPane.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEvent -> {
            ComponentsItem.selectedComponent.set(ComponentsItem.this);
            itemPane.setStyle(mouseHoverStyle);
            itemPane.getChildren().add(0, btnAdd);
        });

        // 鼠标离开 主itemPane
        itemPane.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> {
            itemPane.getChildren().remove(btnAdd);
            itemPane.setStyle(normalStyle);
        });

        // 鼠标双击
        itemPane.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                addComponent();
            }
        });
    }

}
