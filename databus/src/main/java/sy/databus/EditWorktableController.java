package sy.databus;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import sy.databus.organize.ProcessorManager;
import sy.databus.process.AbstractIntegratedProcessor;

public class EditWorktableController {
    public static EditWorktableController INSTANCE = null;
    public Pane editWorktable;
    public Button btnDevFile;
    public Label labelDirPath;
    public ComboBox<Class<? extends AbstractIntegratedProcessor>> cbbFileType;

    public EditWorktableController() {INSTANCE = this;}

    @FXML
    private void initialize() {
        cbbFileType.setItems(ProcessorManager.gReplayFileReaders);
        cbbFileType.setConverter(new StringConverter<>() {
            @Override
            public String toString(Class<? extends AbstractIntegratedProcessor> aClass) {
                // TODO 考虑后续给Processor注解增加name属性，用于中文显示各处理器名称
                return aClass.getSimpleName();
            }

            @Override
            public Class<? extends AbstractIntegratedProcessor> fromString(String s) {
                return null;
            }
        });


    }
}
