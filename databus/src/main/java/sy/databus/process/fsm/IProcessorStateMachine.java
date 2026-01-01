package sy.databus.process.fsm;

public interface IProcessorStateMachine {

    // 开始
    /** 启用当前处理机，使处理器达到RUNNING状态 */
    void open();
    // 暂停
    /** 搁置当前处理机，暂停当前处理器或禁入数据（阻止数据进入业务handler），但不清空已存在任务队列中的待处理任务*/
    void park();
    // 结束
    /** 中止当前处理机的工作，让正在执行中的任务自然结束，清空任务队列中的未处理任务*/
    void close();
    // 禁用（先完成close，再提交ExecutorManager统一处理）
    /** 关闭当前处理机对应的执行器*/
    void shutdown();

}
