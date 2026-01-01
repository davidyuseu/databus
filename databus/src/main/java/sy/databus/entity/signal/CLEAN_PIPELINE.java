package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.entity.STask;
import sy.databus.process.IRouter;

/**
 * 在工作模式切换为编辑模式时，由各源头processor发送该信号，用于检查清理各执行器队列
 * */
public class CLEAN_PIPELINE extends TASK_LOOPBACK {
    public CLEAN_PIPELINE(IRouter<IEvent> source, STask task) {
        super(source, task);
    }
}
