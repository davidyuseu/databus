package sy.databus.process;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DelayPreferenceGroup {

    private Map<Integer, BaseDelayPreference> preferredSelectors = new HashMap<>(); // <设备号, 优选组>

    // 切[任务模式]时在处理器的boot方法中调用
    public void allocateSelectors(int chan, int groupId, int priority) {
        var selector
                = preferredSelectors.computeIfAbsent(groupId, BaseDelayPreference::new);
        selector.putPriority(chan, priority);
    }

    // 新文件任务或时间跳转时调用
    public void resetAll() {
        for (var entry : preferredSelectors.entrySet()) {
            int min = Collections.min(entry.getValue().getPriorities().values());
            entry.getValue().setCrntPriority(min);
            entry.getValue().setLastPreferredTime(0L);
        }
    }

    // 切[编辑模式]时，在处理器中的reset方法中调用
    public void clearAll() {
        preferredSelectors.clear();
    }

    public boolean prefer(int groupId, int chan, long timestamp) {
        var selector = preferredSelectors.get(groupId);
        return selector.preferChan(chan, timestamp);
    }

}
