package sy.databus.organize;

import javafx.scene.Node;
import sy.databus.process.fsm.PopularState;
import sy.databus.process.fsm.SyncObservableState;

import java.util.Set;

public interface ComponentCustomization {
    /**
     * Map<String, Node> nodes0 = controllers.stream().collect(Collectors.toMap(Node::getId, Function.identity()));
     * Map<String, ConsoleController> nodes = controllers.stream().collect(Collectors.toMap(Node::getId, t -> (ConsoleController)t));
     * */

    void customise(Set<Node> controllers);

    void uncustomize(Set<Node> controllers);

    SyncObservableState getCrntState();

    BaseCustomisedChangeListener<PopularState> getCrntStateListener();

    void setCrntStateListener(BaseCustomisedChangeListener<PopularState> listener);


}
