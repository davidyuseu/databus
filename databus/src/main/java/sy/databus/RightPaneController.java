package sy.databus;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import lombok.Getter;
import lombok.Setter;
import sy.databus.global.GlobalState;
import sy.databus.organize.ConsoleManager;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.IEventProc;
import sy.databus.view.controller.ConsoleController;

import java.util.Set;
import java.util.stream.Collectors;

import static sy.databus.process.Console.Category.PROPERTIES;
import static sy.databus.process.Console.Config.NON_RUNNING;
import static sy.databus.view.controller.ConsoleController.STYLE_CLASS_CACHED_CONTROLLER;

public class RightPaneController {

    public static RightPaneController INSTANCE = null;
    @FXML @Getter
    private Accordion outerContainers;
    @FXML @Getter
    private TitledPane comPropertyPane;

    public static void setTitle(String title) {
        INSTANCE.comPropertyPane.setText(title);
    }
    /** 右边栏当前展示的processor*/
    @Getter @Setter
    private AbstractIntegratedProcessor displayedProcessor;
    public void setDisplayedProcessor(AbstractIntegratedProcessor processor) {
        this.displayedProcessor = processor;
        if (displayedProcessor != null) {
            setTitle(displayedProcessor.getNameValue());
        }
    }

    public RightPaneController() {
        INSTANCE = this;
    }

    @FXML
    private void initialize() {

    }
    /**
     * ps：！！以下各获取ConsoleController的方法，能获取到非空Node集合的前提是这些控件已经被show到了界面上！！
     * */
    public static Set<Node> getDisplayedControllers() {
        return RightPaneController.INSTANCE.getOuterContainers().lookupAll("." + STYLE_CLASS_CACHED_CONTROLLER);
    }

    public static Set<Node> getPropertiesControllers() {
        Node propertiesPanel = RightPaneController.INSTANCE.getOuterContainers().lookup("#" + PROPERTIES.getName());
        if (propertiesPanel != null) {
            return propertiesPanel.lookupAll("." + STYLE_CLASS_CACHED_CONTROLLER);
        } else {
            return null;
        }
    }

    public static Node getControllerOfProperties(String id) {
        Node propertiesPanel = RightPaneController.INSTANCE.getOuterContainers().lookup("#" + PROPERTIES.getName());
        if (propertiesPanel != null) {
            return propertiesPanel.lookup("#" + id);
        } else {
            return null;
        }
    }

    public static Set<Node> getConsoleControllers() {
        return RightPaneController.INSTANCE.getOuterContainers().lookupAll("." + STYLE_CLASS_CACHED_CONTROLLER);
    }

    public static Set<ConsoleController> getNonRunningControllers() {
        return getConsoleControllers().stream()
                .filter(node -> node instanceof ConsoleController controller
                        && controller.getAnnoConsole().config() == NON_RUNNING)
                .map(node -> (ConsoleController)node)
                .collect(Collectors.toSet());
    }

    public static Set<Node> getDisplayedControllers(IEventProc proc) {
        return getDisplayedControllers().stream()
                .filter(node -> ((ConsoleController) node).getParentObj() == proc)
                .collect(Collectors.toSet());
    }

    public static void adaptControllers(AbstractIntegratedProcessor processor) {
        GlobalState.recycleRightPaneResources();
        INSTANCE.setDisplayedProcessor(processor);
        ConsoleManager.adaptProcessorController(INSTANCE.outerContainers,
                processor);
    }

    public static boolean isCurrentDisplayed(AbstractIntegratedProcessor processor) {
        return INSTANCE.displayedProcessor != null
                && INSTANCE.displayedProcessor == processor;
    }

}
