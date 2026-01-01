package sy.databus;

import javafx.application.Platform;
import javafx.scene.Node;
import sy.databus.process.Console;
import sy.databus.view.controller.ConsoleController;
import sy.databus.view.controller.IDataVisualization;

import java.util.Set;
import java.util.stream.Collectors;

public class ConsoleRefreshTask extends UITask {

    // TODO 需要考虑call和updateValue方法不在同一个临界区内，
    //  即可能出现call -> xxx增删组件 -> updateValue的调用情况
    protected ConsoleRefreshTask() {
        super(() -> {
            /** 能够被可视化的组件需要满足两个条件：
             * 1. 实现IDataVisualization接口
             * 2. Console注解中的Report属性为RUNTIME
             * */
            synchronized (UISchedules.consoleUILock) {
                Set<Node> nodes = RightPaneController.getDisplayedControllers().stream()
                        .filter(controller ->
                                controller instanceof IDataVisualization
                                        && ((ConsoleController) controller).getAnnoConsole().report()
                                        == Console.Report.RUNTIME
                        ).collect(Collectors.toSet());
                if (!nodes.isEmpty()) {
                    Platform.runLater(() -> {
                        for (var node : nodes) {
                            ((IDataVisualization) node).updateDisplay();
                        }
                    });
                }
            }
        });
    }
}
