package sy.databus;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import sy.databus.process.AbstractMessageProcessor;
import sy.databus.process.IEventProc;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.internal.OriDataViewHandler;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class OriDataViewController {

    public static final int DEFAULT_DATA_VIEW_MAX_LEN = 1024;

    @Getter
    private final Object viewLocker = new Object();
    @Getter
    private IEventProc viewedProc = null;
    @Getter
    private OriDataViewHandler dataViewHandler;
    @Getter
    private Button switchBtn = new Button("暂停");

    private Label lbOffset = new Label("Offset:");
    private TextField tfOffset = new TextField("0");
    private String lastOffsetStr = "0";

    private Label lbMaxLen = new Label("MaxLen:");
    private String lastMaxLenStr = String.valueOf(DEFAULT_DATA_VIEW_MAX_LEN);
    private TextField tfMaxLen= new TextField(lastMaxLenStr);

    private Label title = new Label();
    private HBox titlePane = new HBox(title);

    private HBox ctrlsPane = new HBox(switchBtn, lbOffset, tfOffset, lbMaxLen, tfMaxLen);
    private StackPane topPane = new StackPane(ctrlsPane, titlePane);

    @Getter
    private ListView<String> dataListView = new ListView<>();
    @Getter
    private List<String> dataList = new ArrayList<>();

    private VBox viewPane = new VBox();

    public void init() {
        var obDataList = FXCollections.observableList(dataList);
        dataListView.setItems(obDataList);
        dataListView.setPrefHeight(584d);
//        dataListView.setStyle("-fx-font-family: 'Courier New';"+"-fx-font-size: 14");
        dataListView.getStyleClass().add("oriDataList");

        title.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-weight: bold;");
        title.setPrefHeight(22d);
        titlePane.setPadding(new Insets(0, 0, 0, 4d));
        titlePane.setPickOnBounds(false);

        ctrlsPane.setAlignment(Pos.CENTER_RIGHT);

        switchBtn.setPrefSize(80d, 22d);
        HBox.setMargin(switchBtn, new Insets(0, 4d, 0, 0));
        switchBtn.setOnAction(event -> {
            if (!UISchedules.startOriDataViewTimers())
                UISchedules.stopOriDataViewTimers();
        });

        lbOffset.setPrefHeight(22d);
        lbOffset.setTooltip(new Tooltip("起始显示位置"));
        tfOffset.setPrefSize(80d, 22d);
        tfOffset.focusedProperty().addListener((ob, oldValue, newValue) -> {
            if (newValue != null && !newValue) {
                String input = tfOffset.getText();
                if (lastOffsetStr.equals(input))
                    return;
                if (dataViewHandler == null || input == null || input.equals("")) {
                    setDefaultOffset();
                    return;
                }
                if (input.matches("^[0-9]*[1-9][0-9]*|0$")) {
                    lastOffsetStr = input;
                    dataViewHandler.setOffset(Integer.parseInt(input));
                } else {
                    setDefaultOffset();
                }
            }
        });

        lbMaxLen.setPrefHeight(22d);
        lbMaxLen.setTooltip(new Tooltip("最大显示长度"));
        tfMaxLen.setPrefSize(80d, 22d);
        tfMaxLen.focusedProperty().addListener((ob, oldValue, newValue) -> {
            if (newValue != null && !newValue) {
                String input = tfMaxLen.getText();
                if (lastMaxLenStr.equals(input))
                    return;
                if (dataViewHandler == null || input == null || input.equals("")) {
                    setDefaultMaxLen();
                    return;
                }
                if (input.matches("^[1-9]\\d*$")) {
                    lastMaxLenStr = input;
                    dataViewHandler.setMaxLen(Integer.parseInt(input));
                } else {
                    setDefaultMaxLen();
                }
            }
        });

        ctrlsPane.setSpacing(4d);
        viewPane.setSpacing(4d);
        viewPane.setPadding(new Insets(4d));
        viewPane.getChildren().addAll(topPane, dataListView);
        viewPane.setMinWidth(546d);
        MainPaneController.INSTANCE.getTabOriDataView().setContent(viewPane);
    }

    private void setDefaultOffset() {
        lastOffsetStr = "0";
        tfOffset.setText(lastOffsetStr);
        if (dataViewHandler != null)
            dataViewHandler.setOffset(0);
    }

    private void setDefaultMaxLen() {
        lastMaxLenStr = String.valueOf(DEFAULT_DATA_VIEW_MAX_LEN);
        tfMaxLen.setText(lastMaxLenStr);
        if (dataViewHandler != null)
            dataViewHandler.setMaxLen(DEFAULT_DATA_VIEW_MAX_LEN);
    }

    public void loadIn(AbstractMessageProcessor msgProcessor) {
        dataViewHandler = new OriDataViewHandler(viewLocker, this);
        msgProcessor.addInternalHandler(0, dataViewHandler);
        title.setText(msgProcessor.getNameValue());
        viewedProc = msgProcessor;
        setDefaultOffset();
        setDefaultMaxLen();
    }

    public void loadIn(AbstractTransHandler msgHandler) {
        dataViewHandler = new OriDataViewHandler(viewLocker, this);
        var parentProc = ((AbstractMessageProcessor) msgHandler.getParentProcessor());
        String targetNum = "#" + parentProc.insertHandler(msgHandler, dataViewHandler) + " ";
        title.setText(parentProc.getNameValue() + targetNum + msgHandler.getName() + "(输出)");
        viewedProc = msgHandler;
        setDefaultOffset();
        setDefaultMaxLen();
    }

    public void reloadIn(IEventProc msgProc) {
        synchronized (viewLocker) {
            if (dataViewHandler != null) {
                discardCrntHandler();
                dataListView.getItems().clear();
            }
        }
        if (msgProc instanceof AbstractMessageProcessor msgProcessor) {
            loadIn(msgProcessor);
        } else if (msgProc instanceof AbstractTransHandler msgHandler) {
            if (msgHandler.getParentProcessor() instanceof AbstractMessageProcessor) {
                loadIn(msgHandler);
            } else {
                log.error("This processor type '{}' cannot support original data view!",
                        OriDataViewController.class.getSimpleName());
            }
        } else {
            log.error("This processor type '{}' cannot support original data view!",
                    OriDataViewController.class.getSimpleName());
        }
    }

    public void unload() {
        synchronized (viewLocker) {
            if (dataViewHandler != null) {
                discardCrntHandler();
                dataListView.getItems().clear();
                title.setText("");
                viewedProc = null;
            }
        }
    }

    private void discardCrntHandler() {
        dataViewHandler.setLoaded(false);
        dataViewHandler.getStrQueue().clear();
        if (viewedProc instanceof AbstractMessageProcessor msgProcessor) {
            msgProcessor.removeHandler(dataViewHandler);
        } else if (viewedProc instanceof AbstractTransHandler msgHandler) {
            ((AbstractMessageProcessor) msgHandler.getParentProcessor()).removeHandler(dataViewHandler);
        } else {
            log.error("{} cannot support the processor type!", OriDataViewController.class.getSimpleName());
        }
        dataViewHandler = null;
    }

    public void toggleSwitchBtn(boolean toTurnOn) {
        if (Platform.isFxApplicationThread()) {
            switchBtn.setText(toTurnOn ? "暂停" : "开始");
        } else {
            Platform.runLater(() -> {switchBtn.setText(toTurnOn ? "暂停" : "开始");});
        }
    }

}
