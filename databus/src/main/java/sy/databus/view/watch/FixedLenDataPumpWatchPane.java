package sy.databus.view.watch;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.cell.PropertyValueFactory;
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
import sy.databus.process.analyse.FixedLenDataPump;
import sy.databus.process.analyse.ReadingMode;
import sy.grapheditor.api.GNodeSkin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
@WatchPaneConfig(initialHeight = 360.0, initialWidth = 388.0)
public class FixedLenDataPumpWatchPane extends WatchPane {

    private static final URL location
            = FileReplayerWatchPane.class.getResource("/view/FileReplayerWatchPane.fxml");

    @Getter
    protected final FileReplayerWatchPaneController controller;

    private FileReplayer fileReplayer;

    private final FileChooser fileChooser = new FileChooser();

    private ObservableList<ReplayFileItem> fileList = FXCollections.observableArrayList();

    private RelatedInfo fileRelatedInfo = new RelatedInfo();

    private AtomicBoolean operaFlag = new AtomicBoolean(false);

    @SneakyThrows
    public FixedLenDataPumpWatchPane() {
        FXMLLoader loader = new FXMLLoader(location);
        final Parent root = loader.load();
        controller = loader.getController();

        controller.getNumColumn().setCellValueFactory(new PropertyValueFactory<>("num"));
        controller.getNameColumn().setText("数据文件");
        controller.getNameColumn().setCellValueFactory(new PropertyValueFactory<>("name"));
        controller.getTableTasks().setItems(fileList);

        controller.getLabPath().setText("打开数据文件目录");

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

        controller.getCkbMode().setVisible(false);
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
            if (fileReplayer instanceof FixedLenDataPump fixedLenDataPump) {
                String[] strings = fixedLenDataPump.getExtension().split(";");
                fileChooser.getExtensionFilters().clear();
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("数据", strings));
            }
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
                    fileList.add(replayFileItem);
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
        controller.getBtnPause().setVisible(false);

        // 停止
        controller.getBtnStop().setOnAction(event -> {
            if (ReadingMode.REPLAY == fileReplayer.getMode()) {
                String tip = "任务文件 " + fileReplayer.getIndexToRead() + " 结束回放！";
                controller.getLabelTaskInfo().setText(tip);
            }
            fileReplayer.getCondition().close();
        });

        controller.getSliderTimeline().setVisible(false);
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
    }



    @Override
    public void stopForEditMode() {

    }

    @Override
    public void refresh() {

    }

    @Override
    protected void dissociate() {
        fileReplayer.setFileItems(null);
    }
}
