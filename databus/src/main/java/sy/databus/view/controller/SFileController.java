package sy.databus.view.controller;


import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.common.cache.CacheFactory;
import sy.databus.entity.property.PropertyException;
import sy.databus.entity.property.SFile;
import sy.databus.process.Console;
import sy.databus.view.SLabel;

import java.io.File;
import java.lang.reflect.Field;

/**
 * entity: {@link SFile}
 * */
@Log4j2
public class SFileController extends ConsoleController implements IDataModifier, IDataVisualization {
    protected VBox vBox = new VBox();

    protected SLabel label = new SLabel();

    protected Button btnOpen = new Button("\uF07C");

    protected Label lbPath = new Label();

    protected HBox hBox = new HBox();

    private SFile sFile;

    private FileChooser fileChooser = new FileChooser();

    private DirectoryChooser directoryChooser = new DirectoryChooser();

    public static SFileController buildController(Console console,
                                                  Field field,
                                                  Object obj,
                                                  SFile sFile) {
        SFileController cachedController
                = (SFileController) CacheFactory.getWeakCache(SFileController.class);
        if (cachedController != null) {
            cachedController.rebuild(console, field, obj, sFile);
            return cachedController;
        } else {
            return new SFileController(console, field, obj, sFile);
        }
    }


    @SneakyThrows
    public SFileController(Console console, Field field, Object parentObj, SFile sFile) {
        super(console, field, parentObj);
        this.sFile = sFile;

        if (!console.display().isEmpty())
            label.setText(console.display());
        else
            label.setText(field.getName());


        btnOpen.setPrefWidth(33d);
        btnOpen.setPadding(new Insets(0));
        btnOpen.getStyleClass().add("handlerBtn");
        btnOpen.setFont(Font.font("FontAwesome"));

        lbPath.getStyleClass().add("lbFilePath");
        lbPath.setPrefWidth(203d);
        hBox.getChildren().addAll(btnOpen, lbPath);

        vBox.getChildren().addAll(label, hBox);
        vBox.setSpacing(4d);

        if (this.sFile.isFileFlag()) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(this.sFile.getDescription(), this.sFile.getExtensions()));
            fileChooser.setInitialDirectory(this.sFile.getParentFile());
        } else if (this.sFile.isDirectoryFlag()) {
            directoryChooser.setInitialDirectory(this.sFile.getParentFile());
        } else {
            throw new PropertyException("Unknown SFile!");
        }

        btnOpen.setOnAction(e -> {
            try {
                updateValue(); // update sFile
            } catch (Exception exception) {
                // #- log to view
                abnormalInput();
                log.warn("Failed to change the sFile!", exception);
            }
            updateDisplay();
        });

        this.getChildren().add(vBox);
        updateDisplay();
    }


    @SneakyThrows
    public void rebuild(Console console, Field field, Object parentObj, SFile sFile) {
        super.rebuild(console, field, parentObj);
        this.sFile = sFile;
        if (!console.display().isEmpty())
            label.setText(console.display());
        else
            label.setText(field.getName());

        if (this.sFile.isFileFlag()) {
            fileChooser.getExtensionFilters().remove(0);
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(this.sFile.getDescription(), this.sFile.getExtensions()));
            fileChooser.setInitialDirectory(this.sFile.getParentFile());
        } else if (this.sFile.isDirectoryFlag()) {
            directoryChooser.setInitialDirectory(this.sFile.getParentFile());
        } else {
            throw new PropertyException("Unknown SFile!");
        }

        setUneditable(!editable);
        updateDisplay();
    }

    @Override
    public void setUneditable(boolean uneditable) {
        editable = !uneditable;
        btnOpen.setDisable(uneditable);
    }

    @Override
    protected void normalDisplay() {
        super.normalDisplay();
        lbPath.setStyle("-fx-text-fill: #494649FF");
    }

    @Override
    protected void abnormalInput(){
        super.abnormalInput();
        lbPath.setStyle("-fx-text-fill: red;" +
                "-fx-border-color: red");
    }

    @Override
    protected void abnormalReport() {
        super.abnormalReport();
        lbPath.setStyle("-fx-text-fill: #FF7700FF;" +
                "-fx-border-color: #FF7700FF");
    }


    public void updateDisplay() {
        String path;
        try {
            if (sFile == null || !sFile.exists()) {
                abnormalInput();
                sFile.setValid(false);
                return;
            }
            if (!sFile.isValid()) {
                if (sFile.isActionOnce()) {
                    abnormalInput();
                } else {
                    doFileChangedAction();
                    sFile.setValid(true);
                }
            } else {
                normalDisplay();
            }
            path = getReport();
            lbPath.setText(path);
        } catch (Exception e) {
            sFile.setValid(false);
            abnormalInput();
            log.warn(e);
        }
    }

    protected String getReport() throws Exception{
        if (sFile != null)
            return sFile.getCanonicalPath();
        else throw new Exception("null sFile!");
    }

    @Override
    public void clean() {
        super.clean();
        lbPath.setText("");
    }

    public void updateValue() throws Exception {
        boolean fileChanged = false;
        if (this.sFile.isFileFlag()) {
            File tFile = fileChooser.showOpenDialog(this.getScene().getWindow());
            if (tFile != null && !tFile.getAbsolutePath().equals(this.sFile.getAbsolutePath())) {
                fileChooser.setInitialDirectory(tFile.getParentFile());
                this.sFile = SFile.buildFile(tFile,
                        sFile.getDescription(),
                        sFile.getFileChangedAction(),
                        sFile.isValid(),
                        sFile.isActionOnce(),
                        sFile.getExtensions());
                field.set(parentObj, this.sFile);
                fileChanged = true;
            }
        } else if (this.sFile.isDirectoryFlag()) {
            File tFile = directoryChooser.showDialog(this.getScene().getWindow());
            if (tFile != null && !tFile.getAbsolutePath().equals(this.sFile.getAbsolutePath())) {
                directoryChooser.setInitialDirectory(tFile.getParentFile());
                this.sFile = SFile.buildDirectory(tFile,
                        sFile.getFileChangedAction(),
                        sFile.isValid(),
                        sFile.isActionOnce());
                field.set(parentObj, this.sFile);
                fileChanged = true;
            }
        } else {
            sFile.setValid(false);
            throw new Exception("Unknown flag of SFile!");
        }
        sFile.setValid(true);
        if (fileChanged) {
            doFileChangedAction();
        }
    }

    private void doFileChangedAction() throws Exception {
        sFile.setActionOnce(true);
        try {
            if (this.sFile.getFileChangedAction() != null) {
                this.sFile.getFileChangedAction().changed(this.sFile);
                sFile.setValid(true);
            }
        } catch (Exception e) {
            sFile.setValid(false);
            throw e;
        }
    }

}
