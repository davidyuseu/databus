package sy.databus;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import sy.databus.organize.FileReaderItem;
import sy.databus.organize.ProcessorManager;
import sy.databus.view.utils.AwesomeIcon;

@Deprecated
public class AnalysisWorktableController {
    public static AnalysisWorktableController INSTANCE = null;
    public Pane analysisWorktable;
    public Button btnDisplayFReader;

    public AnalysisWorktableController() {INSTANCE = this;}

    @FXML
    private void initialize() {
        btnDisplayFReader.setGraphic(AwesomeIcon.DISPLAY_FILE_READER.node());
        btnDisplayFReader.getStyleClass().add("awesomeBtn");
        btnDisplayFReader.setFont(Font.font("FontAwesome"));
        btnDisplayFReader.setOnAction(e -> {
            if (ProcessorManager.gFileReader != null)
                FileReaderItem.displayFileReader();
        });
    }

}
