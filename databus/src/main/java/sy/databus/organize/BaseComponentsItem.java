package sy.databus.organize;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;

import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.Setter;
import sy.databus.process.IEventProc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static sy.databus.global.Constants.COLOR_DEFAULT_LINE;

public class BaseComponentsItem<T> extends VBox {
    private static final double ITEM_PANE_HEIGHT = 28d;
    private static final double ITEM_WIDTH = 279d;

    private static final Border BORDER_BINDING_ITEM_PANE = new Border(
            new BorderStroke(Paint.valueOf(COLOR_DEFAULT_LINE), BorderStrokeStyle.SOLID,
            new CornerRadii(0, 0, 0, 6, false), new BorderWidths(0, 0, 1, 1)));

    // 当前ComponentsItem的类，processor或handler
    @Getter
    protected Class<? extends T> opClazz;

    @Getter @Setter
    protected Class<? extends IEventProc> bindingClazz;

    protected HBox itemPane = new HBox();

    protected VBox bindingPane;
    public void addToBindingPane(Node subItem) {
        if (bindingPane == null) {
            bindingPane = new VBox();
            bindingPane.setPadding(new Insets(0, 0, 0, 31.0));
            // 默认隐藏bindingPane
            bindingPane.setVisible(false);
            bindingPane.setManaged(false);
            this.getChildren().add(bindingPane);
        }
        bindingPane.getChildren().add(subItem);
    }


    private static Map<Class, BaseComponentsItem> allComponentsItem = new HashMap<>(64);
    public static BaseComponentsItem getComponent(Class comClazz) {
        return allComponentsItem.get(comClazz);
    }

    public BaseComponentsItem (Class<? extends T> opClazz) {
        this.opClazz = opClazz;
        itemPane.setPrefHeight(ITEM_PANE_HEIGHT);
        HBox.setHgrow(itemPane, Priority.ALWAYS);
        this.setPrefWidth(ITEM_WIDTH);
        this.getChildren().add(itemPane);
        allComponentsItem.put(this.opClazz, this);
    }

    public static void removeComponentsItem(BaseComponentsItem componentsItem) {
        allComponentsItem.remove(componentsItem.getOpClazz(), componentsItem);
    }

    @Getter
    private static Set<BaseComponentsItem> componentsWithBinding = new HashSet<>();
    public static void addComponentWithBinding(BaseComponentsItem componentsItem
            , Class<? extends IEventProc> bindingClazz) {
        componentsItem.itemPane.setBorder(BORDER_BINDING_ITEM_PANE);
        componentsItem.setBindingClazz(bindingClazz);
        componentsWithBinding.add(componentsItem);
    }

}
