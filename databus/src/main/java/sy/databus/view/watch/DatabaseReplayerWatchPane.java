package sy.databus.view.watch;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.SneakyThrows;
import sy.databus.DatabaseReaderWatchPaneController;
import sy.databus.entity.RelatedInfo;
import sy.databus.entity.ReplayTaskItem;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.analyse.DatabaseReplayer;
import sy.grapheditor.api.GNodeSkin;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

import static sy.databus.process.analyse.ReadingMode.REPLAY;

@WatchPaneConfig(initialHeight = 360.0, initialWidth = 388.0)
public class DatabaseReplayerWatchPane extends WatchPane {

    private static final URL location
            = DatabaseReplayerWatchPane.class.getResource("/view/DatabaseReplayerWatchPane.fxml");

    @Getter
    protected final DatabaseReaderWatchPaneController controller;

    private DatabaseReplayer replayer;

    private ObservableList<ReplayTaskItem> taskList = FXCollections.observableArrayList();

    private RelatedInfo taskRelatedInfo = new RelatedInfo();

    private AtomicBoolean operaFlag = new AtomicBoolean(false);

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss:SSS");

    @SneakyThrows
    public DatabaseReplayerWatchPane() {
        FXMLLoader loader = new FXMLLoader(location);
        final Parent root = loader.load();
        controller = loader.getController();

        controller.getNumColumn().setCellValueFactory(new PropertyValueFactory<>("num"));
        controller.getNameColumn().setCellValueFactory(new PropertyValueFactory<>("taskTable"));
        controller.getStartTimeColumn().setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        controller.getTableTasks().setItems(taskList);

        taskList.addListener(
                (ListChangeListener<ReplayTaskItem>) change -> {
                    while (change.next()) {
                        if (taskList.isEmpty()) {
                            controller.setActionOff();
                        } else {
                            controller.setActionOn();
                        }
                    }
                });

        controller.getCkbSingleTask().selectedProperty().addListener((ob, old, newVar) -> {
            replayer.setSingleTask(newVar);
        });

        controller.getCkbMode().addEventFilter(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (!replayer.switchMode()) { // 回放/全速切换失败不能改变CheckBox
                mouseEvent.consume();
            }
        });

        controller.getCkbMode().selectedProperty().addListener((ob, old, newVar) -> {
            if (newVar) {
                if (replayer.getMode() == REPLAY) {
                    controller.getSliderTimeline().setDisable(false);
                    controller.getLbStartDate().setVisible(true);
                    controller.getLbEndDate().setVisible(true);
                    controller.getLabTimeProcess().setVisible(true);
                }
            } else {
                if (replayer.getMode() != REPLAY) {
                    controller.getSliderTimeline().setDisable(true);
                    controller.getLbStartDate().setVisible(false);
                    controller.getLbEndDate().setVisible(false);
                    controller.getLabTimeProcess().setVisible(false);
                }
            }
        });

        taskRelatedInfo.getTaskTip().addListener((observableValue, s, t1) -> {
            Platform.runLater(()-> {
                controller.getLabelTaskInfo().setText(t1);
            });
        });

        taskRelatedInfo.getTaskTitle().addListener((ob, old, newTitle) -> {
            Platform.runLater(()-> {
                controller.getLbCrntTask().setText(newTitle);
            });
        });

        // 刷新任务列表
        controller.getBtnRefreshTasks().setOnAction(e -> {
            LocalDate startDate = controller.getDpStartDate().getValue();
            LocalDate endDate = controller.getDpEndDate().getValue();
            if (startDate == null) {
                startDate = LocalDate.now();
                controller.getDpStartDate().setValue(startDate);
            }
            if (endDate == null || endDate.isBefore(startDate)) {
                endDate = startDate.withDayOfYear(startDate.lengthOfYear());
                controller.getDpEndDate().setValue(endDate);
            }
            // 先同步中止当前任务
            replayer.getCondition().close();
            replayer.refreshTasks(startDate, endDate);
        });
        // 开始
        controller.getBtnStart().setOnAction(event -> {
            if (taskList == null || taskList.isEmpty())
                return;

            int selIdx = controller.getTableTasks().
                    getSelectionModel().getSelectedIndex();
            selIdx = Math.max(selIdx, 0);
            if (selIdx != replayer.getIndexToRead()) {
                replayer.getCondition().close();
                replayer.setIndexToRead(selIdx);
            }
            replayer.getCondition().open();
        });
        // 暂停
        controller.getBtnPause().setOnAction(event -> {
            replayer.getCondition().park();
        });
        // 结束
        controller.getBtnStop().setOnAction(event -> {
            ReplayTaskItem crntTask = replayer.getCrntTaskItem();
            if (crntTask != null) {
                controller.getLabelTaskInfo()
                        .setText("任务 '" + crntTask.getTaskTable() + "' 结束回放！" );
            }
            replayer.getCondition().close();
        });

        controller.getSliderTimeline().addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (replayer.getCondition().isRunnable()
                    || replayer.getCondition().isTerminated()) {
                mouseEvent.consume();
                return;
            }
            operaFlag.compareAndSet(false, true);
        });

        controller.getSliderTimeline().addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            if (replayer.getCondition().isRunnable()
                    || replayer.getCondition().isTerminated()) {
                mouseEvent.consume();
                return;
            }
            toJump();
        });

        controller.getSliderTimeline().focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (replayer.getCondition().isRunnable()
                    || replayer.getCondition().isTerminated()) {
                return;
            }
            if (!t1) {
                toJump();
            }
        });

        controller.getSliderTimeline().valueProperty().addListener((observableValue, number, t1) -> {
            if (replayer.getCondition().isRunnable()
                    || replayer.getCondition().isTerminated()) {
                return;
            }
            controller.getLabTimeProcess()
                    .setText(timeFormat.format(t1.longValue()));
        });

        this.addToContent(root);
    }

    private void toJump() {
        if (operaFlag.compareAndSet(true, false)) {
            long sldValue = (long) controller.getSliderTimeline().getValue();
            if (sldValue != replayer.getLt0()) {
                replayer.jumpTo(sldValue);
            }
        }
    }

    @Override
    public void refresh() {
    }

    @Override
    public void embeddedInRootNodeSkin(GNodeSkin rootNodeSkin) {
        super.embeddedInRootNodeSkin(rootNodeSkin);
        rootSkin.getRoot().setDraggedResizable(false);
    }

    @Override
    public void associateWith(AbstractIntegratedProcessor processor) {
        super.associateWith(processor);
        replayer = (DatabaseReplayer) integratedProcessor;
        replayer.setTaskItems(taskList);
        replayer.getCrntStartTime().addListener(this::startTimeChanged);
        replayer.getCrntEndTime().addListener(this::endTimeChanged);
        replayer.getCrntChangedTime().addListener(this::crntTimeChanged);

    }

    @Override
    protected void dissociate() {
        replayer.setTaskItems(null);
        replayer.getCrntStartTime().removeListener(this::startTimeChanged);
        replayer.getCrntEndTime().removeListener(this::endTimeChanged);
        replayer.getCrntChangedTime().removeListener(this::crntTimeChanged);
    }

    @Override
    public void stopForEditMode() {
        taskList.clear();
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
