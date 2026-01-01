package sy.databus.view.watch;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import sy.common.cache.ICache;
import sy.databus.MainPaneController;
import sy.databus.RightPaneController;
import sy.databus.global.GlobalState;
import sy.databus.global.WorkMode;
import sy.databus.organize.ConsoleManager;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.AbstractMessageProcessor;
import sy.databus.view.customskins.titled.TitledNodeSkin;
import sy.grapheditor.api.GNodeSkin;

import static sy.databus.view.customskins.titled.TitledNodeSkin.CONTENTROOT_FIXED_INSETS;

/**
 * ps:
 * 1. 所有的WatchPane继承VBox布局
 * 2. 所有的WatchPane实现IProcessorPane接口
 * 3. 所有的WatchPane实现缓存接口ICache
 * */
@Log4j2
public abstract class WatchPane extends VBox implements IProcessorPane {

    public static final double HEADER_HEIGHT = 25;

    // content panel
    @Getter
    protected VBox contentLayout = new VBox();
    // title
    @Getter
    private final Label titleLabel = new Label();
    // header
    protected BorderPane headerBox = new BorderPane(titleLabel);
    @Getter
    protected AbstractIntegratedProcessor integratedProcessor;

    /**--embeddedInRootNodeSkin--------------------------------------------------------------------------------------*/
    protected TitledNodeSkin rootSkin = null;

    private ChangeListener<? super String> titleListener = null;

    protected ChangeListener<WorkMode> workModeListener = this::workModeChanged;

    protected void workModeChanged(ObservableValue<? extends WorkMode> observableValue,
                                   WorkMode workMode, WorkMode newValue) {
        for (Node node : contentLayout.getChildren()) {
            node.setDisable(newValue == WorkMode.EDIT);
        }
    }

    public WatchPane() {
        this.getStyleClass().add("watchPane");

        titleLabel.getStyleClass().add("titleLabel");
        titleLabel.setPrefWidth(Double.MAX_VALUE);

        headerBox.getStyleClass().add("header");
        headerBox.setMinHeight(HEADER_HEIGHT);
        headerBox.setMaxHeight(HEADER_HEIGHT);

        contentLayout.getStyleClass().add("content");
        VBox.setVgrow(contentLayout, Priority.ALWAYS);

        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::filterMouseDragged);

        this.setMinHeight(HEADER_HEIGHT + CONTENTROOT_FIXED_INSETS * 2 + 1);
        this.getChildren().addAll(headerBox, contentLayout);

        GlobalState.currentWorkMode.addListener(new WeakChangeListener<>(workModeListener));

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    if (RightPaneController.isCurrentDisplayed(this.integratedProcessor))
                        return;

                    RightPaneController.adaptControllers(this.integratedProcessor);
                    GlobalState.setCurrentNodeSkin(rootSkin);
                }
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    MainPaneController.reloadOriDataView(this.integratedProcessor);
                }
            }
        });
    }

    public void addToContent(Node node) {
        contentLayout.getChildren().add(node);
    }

    public void addAllToContent(Node... nodes){
        contentLayout.getChildren().addAll(nodes);
    }

    /**
     * Stops the node being dragged if it isn't selected.
     *
     * @param event a mouse-dragged event on the node
     */
    private void filterMouseDragged(final MouseEvent event) {
        if (event.isPrimaryButtonDown() && !rootSkin.isSelected()) {
            event.consume();
        }
    }

    abstract protected void dissociate();

    @Override
    public void release() {
        dissociate();
        rootSkin = null;
        integratedProcessor = null;
        titleListener = null;
    }

    /** TitledNodeSkin 将已经完成UI构建的WatchPane嵌入其中
     * => 得到GNodeSkin rootSkin*/
    @Override
    public void embeddedInRootNodeSkin(GNodeSkin rootNodeSkin) {
        if (rootNodeSkin instanceof TitledNodeSkin) {

            /** Key step! requires subclasses to implement polymorphically! */
            rootSkin = (TitledNodeSkin) rootNodeSkin;

            contentLayout.prefHeightProperty().bind(this.heightProperty().subtract(headerBox.heightProperty()));

            workModeChanged(GlobalState.currentWorkMode, null, GlobalState.currentWorkMode.get());
        } else {
            log.error(this.getClass().getSimpleName() + " cannot adapt the 'GNodeSkin' - {}!",
                    rootNodeSkin.getClass().getSimpleName());
        }
    }
    /** TitledNodeSkin 将业务处理processor赋予WatchPane
     * => 得到integratedProcessor*/
    @Override
    public void associateWith(AbstractIntegratedProcessor processor) {
        this.integratedProcessor = processor;

        titleListener = (Observable, oldValue, newValue) -> {
            if(newValue != null) {
                titleLabel.setText(newValue);
                if (RightPaneController.INSTANCE.getDisplayedProcessor()
                        == this.integratedProcessor) {
                    RightPaneController.setTitle(newValue);
                }
            }
        };

        this.titleLabel.setText(integratedProcessor.getName().get());
        var weakListener = new WeakChangeListener<>(titleListener);
        this.integratedProcessor.getName().addListener(weakListener);
    }

    abstract public void stopForEditMode();

}
