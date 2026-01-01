package sy.databus.view.watch;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.databus.FileReplayerWatchPaneController;
import sy.databus.entity.ReplayFileItem;
import sy.databus.entity.RelatedInfo;
import sy.databus.organize.TaskManager;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.analyse.FileReplayer;
import sy.databus.process.analyse.ReadingMode;
import sy.grapheditor.api.GNodeSkin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public abstract class FileReplayerWatchPane extends WatchPane{

    private static final URL location
            = FileReplayerWatchPane.class.getResource("/view/FileReplayerWatchPane.fxml");

    @Getter
    protected final FileReplayerWatchPaneController controller;

    private FileReplayer fileReplayer;

    private final FileChooser fileChooser = new FileChooser();

    private ObservableList<ReplayFileItem> fileList = FXCollections.observableArrayList();

    private ChangeListener<Number> selItemListener;

    private RelatedInfo fileRelatedInfo = new RelatedInfo();

    private AtomicBoolean operaFlag = new AtomicBoolean(false);

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss:SSS");

    protected abstract ReplayFileItem organizeFileInfo (ReplayFileItem fileItem);

    @SneakyThrows
    public FileReplayerWatchPane() {
        FXMLLoader loader = new FXMLLoader(location);
        final Parent root = loader.load();
        controller = loader.getController();

        controller.getNumColumn().setCellValueFactory(new PropertyValueFactory<>("num"));
        controller.getNameColumn().setCellValueFactory(new PropertyValueFactory<>("name"));
        controller.getTableTasks().setItems(fileList);

        fileList.addListener(
                (ListChangeListener<ReplayFileItem>) change -> {
                    while (change.next()) {
                        if (fileList.isEmpty()) {
                            controller.setActionOff();
                        } else {
                            controller.setActionOn();
                        }
                    }
                });

        controller.getCkbMode().addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (!fileReplayer.switchMode()) { // 回放/全速切换失败不能改变CheckBox
                mouseEvent.consume();
            }
        });

        controller.getCkbMode().selectedProperty().addListener((ob, old, newVar) -> {
            if (newVar) {
                if (fileReplayer.getMode() == ReadingMode.REPLAY) {
                    controller.getSliderTimeline().setDisable(false);
                    controller.getLbStartDate().setVisible(true);
                    controller.getLbEndDate().setVisible(true);
                    controller.getLabTimeProcess().setVisible(true);
                }
            } else {
                if (fileReplayer.getMode() != ReadingMode.REPLAY) {
                    controller.getSliderTimeline().setDisable(true);
                    controller.getLbStartDate().setVisible(false);
                    controller.getLbEndDate().setVisible(false);
                    controller.getLabTimeProcess().setVisible(false);
                }
            }
        });

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("回放数据", "*.dat"));

        fileRelatedInfo.getTaskTip().addListener((observableValue, s, t1) -> {
            Platform.runLater(()-> {
                controller.getLabelTaskInfo().setText(t1);
            });
        });

        fileRelatedInfo.getTaskTitle().addListener((ob, old, newTitle) -> {
            Platform.runLater(()-> {
                controller.getLbCrntTask().setText(newTitle);
            });
        });

        // 打开分析文件目录
        controller.getBtnOpen().setOnAction(e -> {
            List<File> files = fileChooser.showOpenMultipleDialog(this.getScene().getWindow());
            if (files != null && !files.isEmpty()) {
                fileReplayer.getCondition().close();
                fileList.clear();
                for (int i = 0; i < files.size(); i++) {
                    if (i == 0) {
                        try {
                            controller.getLabPath()
                                    .setText(files.get(i).getParentFile().getCanonicalPath());
                        } catch (IOException ioException) {
                            log.warn(e);
                            continue;
                        }
                    }
                    var replayFileItem = new ReplayFileItem(i,
                            files.get(i).getName(),
                            files.get(i),
                            fileRelatedInfo);
                    fileList.add(organizeFileInfo(replayFileItem));
                }
            }
        });

        // 开始
        controller.getBtnStart().setOnAction(event -> {
            if (ReadingMode.ALLIN == fileReplayer.getMode()) { // 全速模式
                ReplayFileItem crntFileTask = fileList.get(fileReplayer.getIndexToRead());
                if (!TaskManager.isCompleted(crntFileTask.getUuid())
                        && !fileReplayer.getCondition().isSuspended()) {
                    String tip = "序号为" + crntFileTask.getNum() + "的任务正在记录结果，请稍等！";
                    controller.getLabelTaskInfo().setText(tip);
                    return;
                }
            }
            int selIndex = controller.getTableTasks().getSelectionModel().getSelectedIndex();
            selIndex = Math.max(selIndex, 0);
            if (selIndex != fileReplayer.getIndexToRead()) {
                // 所选文件与正在进行的任务不一致，先关闭正在进行的任务
                fileReplayer.getCondition().close();
                fileReplayer.setIndexToRead(selIndex);
            }

            fileReplayer.getCondition().open();
        });

        // 暂停
        controller.getBtnPause().setOnAction(event -> {
            fileReplayer.getCondition().park();
        });

        // 停止
        controller.getBtnStop().setOnAction(event -> {
            if (ReadingMode.REPLAY == fileReplayer.getMode()) {
                String tip = "任务文件 " + fileReplayer.getIndexToRead() + " 结束回放！";
                controller.getLabelTaskInfo().setText(tip);
            }
            fileReplayer.getCondition().close();
        });

        controller.getSliderTimeline().addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (fileReplayer.getCondition().isRunnable()
                    || fileReplayer.getCondition().isTerminated()) {
                mouseEvent.consume();
                return;
            }
            operaFlag.compareAndSet(false, true);
        });

        controller.getSliderTimeline().addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            if (fileReplayer.getCondition().isRunnable()
                    || fileReplayer.getCondition().isTerminated()) {
                mouseEvent.consume();
                return;
            }
            if (operaFlag.compareAndSet(true, false)) {
                long crntSliderValue = (long) controller.getSliderTimeline().getValue();
                if (crntSliderValue != fileReplayer.getLastFileTime()) {
                    fileReplayer.skipToTimestamp(crntSliderValue);
                }
            }
        });

        controller.getSliderTimeline().focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (fileReplayer.getCondition().isRunnable()
                    || fileReplayer.getCondition().isTerminated()) {
                return;
            }
            if (!t1) {
                if (operaFlag.compareAndSet(true, false)) {
                    long crntSliderValue = (long) controller.getSliderTimeline().getValue();
                    if (crntSliderValue != fileReplayer.getLastFileTime()) {
                        fileReplayer.skipToTimestamp(crntSliderValue);
                    }
                }
            }
        });

        controller.getSliderTimeline().valueProperty().addListener((observableValue, number, t1) -> {
            if (fileReplayer.getCondition().isRunnable()
                    || fileReplayer.getCondition().isTerminated()) {
                return;
            }
            controller.getLabTimeProcess().setText(timeFormat.format(t1.longValue()));
        });

        this.addToContent(root);
    }

    @Override
    public void embeddedInRootNodeSkin(GNodeSkin rootNodeSkin) {
        super.embeddedInRootNodeSkin(rootNodeSkin);
        rootSkin.getRoot().setDraggedResizable(false);
    }

    @Override
    public void associateWith(AbstractIntegratedProcessor processor) {
        super.associateWith(processor);

        fileReplayer = (FileReplayer) integratedProcessor;

        fileReplayer.setFileItems(fileList);

        fileReplayer.getCrntStartTime().addListener(this::startTimeChanged);

        fileReplayer.getCrntEndTime().addListener(this::endTimeChanged);

        fileReplayer.getCrntChangedTime().addListener(this::crntTimeChanged);
    }

    @Override
    public void stopForEditMode() {
        fileList.clear();
        controller.getLabPath().setText("");
    }

    @Override
    public void refresh() {

    }

    @Override
    protected void dissociate() {
        fileReplayer.setFileItems(null);
        fileReplayer.getCrntStartTime().removeListener(this::startTimeChanged);
        fileReplayer.getCrntEndTime().removeListener(this::endTimeChanged);
        fileReplayer.getCrntChangedTime().removeListener(this::crntTimeChanged);
    }

    private void startTimeChanged(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        Platform.runLater(() -> {
            if (t1.longValue() > 0) {
                controller.getSliderTimeline().setMin(t1.doubleValue());
                controller.getLbStartDate().setText(dateFormat.format(t1.longValue()));
            } else {
                controller.getLbStartDate().setText("");
            }
        });
    }

    private void endTimeChanged(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        Platform.runLater(() -> {
            if (t1.longValue() > 0) {
                controller.getSliderTimeline().setMax(t1.doubleValue());
                controller.getLbEndDate().setText(dateFormat.format(t1.longValue()));
            } else {
                controller.getLbEndDate().setText("");
            }
        });
    }

    private void crntTimeChanged(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        Platform.runLater(() -> {
            if (operaFlag.compareAndSet(false, true)) {
                if (t1.longValue() > 0) {
                    controller.getSliderTimeline().setValue(t1.doubleValue());
                    controller.getLabTimeProcess().setText(timeFormat.format(t1.longValue()));
                } else {
                    controller.getLabTimeProcess().setText("");
                }
                operaFlag.set(false);
            }
        });
    }
}
