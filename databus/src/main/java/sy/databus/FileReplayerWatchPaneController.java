package sy.databus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import sy.databus.global.Constants;

import java.awt.*;
import java.io.File;
import java.io.IOException;

@Log4j2
public class FileReplayerWatchPaneController {
    @FXML
    @Getter
    public Label lbCrntTask;
    @FXML
    @Getter
    private Label lbStartDate;
    @FXML
    @Getter
    private Label lbEndDate;
    @FXML
    @Getter
    private CheckBox ckbMode;
    @FXML
    @Getter
    private Slider sliderTimeline;
    @FXML
    @Getter
    private Label labTimeProcess;
    @FXML
    @Getter
    private Button btnResult;
    @FXML
    @Getter
    private Label labelTaskInfo;
    @FXML
    @Getter
    private Button btnOpen;
    @FXML
    @Getter
    private Label labPath;
    @FXML
    @Getter
    private TableColumn numColumn;
    @FXML
    @Getter
    private TableColumn nameColumn;
    @FXML
    @Getter
    private Pane basePane;
    @FXML
    @Getter
    private Button btnStart;
    @FXML
    @Getter
    private Button btnPause;
    @FXML
    @Getter
    private Button btnStop;
    @FXML
    @Getter
    private TableView tableTasks;

    @FXML
    private void initialize() {

    }

    public void setActionOn() {
        btnStart.setDisable(false);
        btnPause.setDisable(false);
        btnStop.setDisable(false);
    }

    public void setActionOff() {
        btnStart.setDisable(true);
        btnPause.setDisable(true);
        btnStop.setDisable(true);
    }

    public void openExcelDir(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().open(new File(Constants.TABLE_TXT_DIR_PATH));
        } catch (IOException e) {
            log.error(e);
        }
    }
}
