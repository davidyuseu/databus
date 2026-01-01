package sy.databus.organize;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import sy.databus.RightPaneController;
import sy.databus.process.fsm.PopularState;
import sy.databus.view.controller.ConsoleController;

import java.util.Set;
import java.util.stream.Collectors;

import static sy.databus.process.Console.Config.NON_RUNNING;

/**
 * 插件类，辅助类
 * */
public class ProcessorHelper {

    public static void customise (ComponentCustomization processor, Set<Node> controllers) {
        Set<ConsoleController> nonRunningNodes = controllers.stream()
                .filter(node -> node instanceof ConsoleController controller
                        && controller.getAnnoConsole().config() == NON_RUNNING)
                .map(node -> (ConsoleController)node)
                .collect(Collectors.toSet());
        synchronized (processor.getCrntState().getStateLocker()) {
            if (processor.getCrntState().get() == PopularState.RUNNING) {
                for (var node : nonRunningNodes) {
                    node.setDisable(true);
                }
            } else {
                for (var node : nonRunningNodes) {
                    node.setDisable(false);
                }
            }
            BaseCustomisedChangeListener<PopularState> stateListener = new BaseCustomisedChangeListener<>(processor) {
                @Override
                protected void syncChanged(ObservableValue<? extends PopularState> observableValue,
                                           PopularState popularState, PopularState newState) {
                    var crls = RightPaneController.getNonRunningControllers();
                    if(crls.size() == 0) {
                        return;
                    }
                    Platform.runLater(() -> {
                        for (var crl : crls) {
                            crl.setDisable(newState == PopularState.RUNNING);
                        }
                    });
                }
            };
            processor.setCrntStateListener(stateListener);
            var weakChangeListener = new WeakChangeListener<>(stateListener);
            processor.getCrntState().addListener(weakChangeListener);
        }
    }

    public static void uncustomize(ComponentCustomization component, Set<Node> controllers) {
        component.getCrntStateListener().setCustomised(false);// 标记当前监听器已失活，等待释放
        component.setCrntStateListener(null);
    }
}
