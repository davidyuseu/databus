package sy.databus.process;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


@Log4j2
public class BaseDelayPreference {
    private static final int FIXED_DELAY = 500;
    @Getter
    private final int groupId;
    @Getter
    private Map<Integer, Integer> priorities =  new HashMap<>();

    public void putPriority(int chan, int prior) {
        priorities.put(chan, prior);
    }

    public BaseDelayPreference(int id) {
        this.groupId = id;
    }

    @Getter @Setter
    private int crntPriority;
    @Getter @Setter
    private long lastPreferredTime;

    public boolean preferChan(int chan, long timestamp) { // by delay
        boolean result;
        int chanPriority = priorities.get(chan);
        if (chanPriority == crntPriority) {
            result = true;
        } else if (chanPriority < crntPriority) {
            log.info("从优先级为{}的设备切换至优先级为{}的设备！", crntPriority, chanPriority);
            crntPriority = chanPriority;
            result = true;
        } else { // chanPriority > crntPriority
            if (timestamp - lastPreferredTime > FIXED_DELAY) {
                log.info("从优先级为{}的设备切换至优先级为{}的设备！", crntPriority, chanPriority);
                crntPriority = chanPriority;
                result = true;
            } else {
                return false;
            }
        }
        lastPreferredTime = timestamp;
        return result;
    }

}
