package sy.databus.organize;

import javafx.scene.control.Accordion;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.SneakyThrows;
import sy.databus.MainPaneController;
import sy.databus.RightPaneController;
import sy.databus.entity.ProcessorId;
import sy.databus.global.GlobalState;
import sy.databus.global.InitConfig;
import sy.databus.process.AbstractIntegratedProcessor;


import static sy.databus.global.Constants.COLOR_ITEM_NAME;

@Deprecated
public class FileReaderItem extends BaseComponentsItem<AbstractIntegratedProcessor> {

    @Getter
    private RadioButton radioButton = new RadioButton();

    // 全局RadioGroup
    private static ToggleGroup readerToggleGroup = new ToggleGroup();

    public static void displayFileReader() {
        Accordion outContainers = RightPaneController.INSTANCE.getOuterContainers();
        MainPaneController.clearSelection();
        AbstractIntegratedProcessor displayedProcessor = RightPaneController.INSTANCE.getDisplayedProcessor();
        if (displayedProcessor == null ||
                displayedProcessor != ProcessorManager.gFileReader) {
            GlobalState.recycleRightPaneResources();
            ConsoleManager.adaptProcessorController(outContainers, ProcessorManager.gFileReader);
            RightPaneController.INSTANCE.setDisplayedProcessor(ProcessorManager.gFileReader);
        }
    }


    public FileReaderItem(Class<? extends AbstractIntegratedProcessor> opClazz) {
        super(opClazz);

        radioButton.setText(opClazz.getSimpleName());
        radioButton.setTextFill(Paint.valueOf(COLOR_ITEM_NAME));
        radioButton.getStyleClass().add("readerItem");
        radioButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (radioButton.isSelected()) {
                displayFileReader();
            }
        });

        itemPane.setSpacing(6.0);
        HBox.setHgrow(radioButton, Priority.ALWAYS);
        itemPane.getChildren().add(radioButton);

        radioButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                Accordion outContainers = RightPaneController.INSTANCE.getOuterContainers();
                MainPaneController.INSTANCE.getGraphEditor()
                        .getSelectionManager().clearSelection();
                // 若当前的文件读取器与选中的toggle不同，则注销该读取器
                if (ProcessorManager.gFileReader != null) {
                    ProcessorManager.gFileReader.clear();
                }
                ProcessorManager.gFileReader = createFileReader(this.opClazz);
                ConsoleManager.adaptProcessorController(outContainers, ProcessorManager.gFileReader);
                if (bindingPane != null) {
                    bindingPane.setVisible(true);
                    bindingPane.setManaged(true);
                }
            } else {
                if (bindingPane != null) {
                    bindingPane.setVisible(false);
                    bindingPane.setManaged(false);
                }
            }
        });

        readerToggleGroup.getToggles().add(radioButton);
    }

    @SneakyThrows
    private static AbstractIntegratedProcessor createFileReader(Class<? extends AbstractIntegratedProcessor> processorClass) {
        // 创建综合处理器实例
        AbstractIntegratedProcessor processor = InitConfig.getInstance().getSingleFileReader();
        if (processor == null || !processorClass.isAssignableFrom(processor.getClass())) {
            // 根据该类类型分配PId
            ProcessorId pId = ProcessorManager.allocateProcessorId(processorClass);
            processor = processorClass.getDeclaredConstructor().newInstance();
            processor.bindProcessorId(pId);
            processor.initialize();
            InitConfig.getInstance().setSingleFileReader(processor);
        }
        return processor;
    }

}
