package sy.databus.view.watch;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import lombok.Getter;
import sy.common.tmresolve.ResultStruct;
import sy.databus.MainPaneController;
import sy.databus.organize.BaseCustomisedChangeListener;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.ProcessorInitException;
import sy.databus.process.analyse.ParamsCollector;
import sy.databus.process.fsm.PopularState;
import sy.grapheditor.api.GNodeSkin;

/**
 * {@link ParamsCollector}
 * */
@WatchPaneConfig(initialHeight = 70.0, initialWidth = 118.0)
public class ParamsCollectorWatchPane extends WatchPane {
    private Label sticker = new Label("\uF080");

    private Tab tab = new Tab();

    private VBox vRoot = new VBox();
    private AnchorPane  topPane = new AnchorPane();
    private ToggleGroup togGroup = new ToggleGroup();
    private RadioButton togWatch = new RadioButton("监视");
    private RadioButton togRecord = new RadioButton("记录");

    @Getter
    private TableColumn<ResultStruct, String> nameColumn = new TableColumn<>("参数名");
    @Getter
    private TableColumn<ResultStruct, String> valueColumn = new TableColumn<>("值");
    @Getter
    private TableColumn<ResultStruct, String> unitColumn = new TableColumn<>("单位");

    private TableView<ResultStruct> tableView = new TableView<>();

    private BaseCustomisedChangeListener<PopularState> crntStateListener = null;

    public ParamsCollectorWatchPane() {
        this.getStyleClass().add("paramsCollectorWP");
        headerBox.getStyleClass().add("collectorHeader");
        sticker.getStyleClass().add("sticker");
        sticker.setFont(Font.font("FontAwesome"));
        addToContent(sticker);
        contentLayout.setAlignment(Pos.CENTER);

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("strResult"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        tableView.setPrefHeight(584d);
        tableView.getColumns().addAll(nameColumn, valueColumn, unitColumn);
        nameColumn.prefWidthProperty().bind(tableView.widthProperty().divide(3));
        valueColumn.prefWidthProperty().bind(tableView.widthProperty().divide(3));
        unitColumn.prefWidthProperty().bind(tableView.widthProperty().divide(3));


        togGroup.getToggles().addAll(togWatch, togRecord);
        topPane.getChildren().addAll(togWatch, togRecord);

        AnchorPane.setLeftAnchor(togWatch, 2d);
        AnchorPane.setBottomAnchor(togWatch, 4d);
        AnchorPane.setLeftAnchor(togRecord, 62d);
        AnchorPane.setBottomAnchor(togRecord, 4d);
        vRoot.getChildren().addAll(topPane, tableView);
        vRoot.setPadding(new Insets(8d));
        vRoot.setSpacing(4d);

        tab.setContent(vRoot);

        togGroup.selectedToggleProperty().addListener((ob, old, newTog) -> {
            ((ParamsCollector) this.integratedProcessor).setToRecord(newTog == togRecord);
        });
    }

    @Override
    public void embeddedInRootNodeSkin(GNodeSkin rootNodeSkin) {
        super.embeddedInRootNodeSkin(rootNodeSkin);
        /** 必须在embeddedInRootNodeSkin方法中，待嵌入rootSkin后才可操作rootSkin，确保其不为null*/
        // 设置FrameProcessorPane的初始高度
//        rootSkin.getItem().setHeight(DEFAULT_PANE_HEIGHT); // *不能这样设置，否则回退时由于高度改变了，会出现灰色背景
        rootSkin.getRoot().setDraggedResizable(true);

        MainPaneController.INSTANCE.getTabPaneParamsViews().getTabs().add(tab);
        MainPaneController.INSTANCE.getTabPaneParamsViews().getSelectionModel().select(tab);
        MainPaneController.INSTANCE.selectParamsView();
    }

    @Override
    public void associateWith(AbstractIntegratedProcessor processor) {
        super.associateWith(processor);
        if (this.integratedProcessor instanceof ParamsCollector paramsCollector) {
            if (paramsCollector.isToRecord())
                togGroup.selectToggle(togRecord);
            else
                togGroup.selectToggle(togWatch);
            tableView.setItems(paramsCollector.getSumParams());
        } else {
            throw new ProcessorInitException("'" + ParamsCollectorWatchPane.class.getSimpleName()
                    + "' must adapt '"
                    + ParamsCollector.class.getSimpleName() + "'!");
        }
        tab.textProperty().bind(this.integratedProcessor.getName());

        crntStateListener = new BaseCustomisedChangeListener<>(ParamsCollectorWatchPane.this) {
            @Override
            public void syncChanged(ObservableValue<? extends PopularState> observableValue, PopularState oldState, PopularState newState) {
                if (newState == PopularState.RUNNING) {
                    Platform.runLater(() -> {
                        togRecord.setDisable(true);
                        togWatch.setDisable(true);
                    });
                } else {
                    Platform.runLater(() -> {
                        togRecord.setDisable(false);
                        togWatch.setDisable(false);
                    });
                }
            }
        };

        var weakChangeListener = new WeakChangeListener<>(crntStateListener);
        this.integratedProcessor.getCrntState().addListener(weakChangeListener);
    }

    @Override
    protected void dissociate() {
        tableView.setItems(null);
        tab.textProperty().unbind();
        MainPaneController.INSTANCE.getTabPaneParamsViews().getTabs().remove(tab);
        crntStateListener.setCustomised(false);
        crntStateListener = null;
    }

    @Override
    public void stopForEditMode() {

    }

    @Override
    public void refresh() {
        tableView.refresh();
    }
}
