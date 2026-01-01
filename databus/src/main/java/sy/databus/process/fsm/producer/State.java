package sy.databus.process.fsm.producer;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class State {

    @Getter
    protected final String name;

    public State(String name) {
        this.name = name;
    }

    public enum ActionType {
        OPEN,
        PARK,
        CLOSE,
        SHUTDOWN
    }

    private Map<ActionType, Switch> actions = new HashMap<>();

    public void addAction(ActionType type, Switch action) {
        actions.put(type, action);
    }

    public Switch getAction(ActionType type) {
        return actions.get(type);
    }

    public void doAction(ActionType type) {
        actions.get(type).action();
    }

    public boolean containsAction(ActionType type) {
        return actions.get(type) != null;
    }
}
