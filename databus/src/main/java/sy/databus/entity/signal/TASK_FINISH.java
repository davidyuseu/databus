package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.entity.STask;
import sy.databus.process.IRouter;

import static sy.databus.process.AbstractIntegratedProcessor.RoutingPattern.ALWAYS_TRANSITIVE;

public class TASK_FINISH extends TASK_SIG {

    protected TASK_FINISH(IRouter<IEvent> source, STask task) {
        /** 任务终结信号应当能巡历source的整个处理路径 */
        super(source, ALWAYS_TRANSITIVE, task);
    }
}
