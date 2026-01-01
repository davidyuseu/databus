package sy.databus.process.fsm.producer;


import static sy.databus.process.fsm.producer.State.ActionType.OPEN;
import static sy.databus.process.fsm.producer.State.ActionType.SHUTDOWN;

public class RUNNABLE extends State {

    public RUNNABLE(Switch toOpen, Switch toShutdown){
        super("RUNNABLE");
        /** 注意toOpen方法须提交异步处理后尽快返回，否则会一直处于RUNNABLE状态，并霸占procMutex锁*/
        addAction(OPEN, toOpen);
        addAction(SHUTDOWN, toShutdown);
    }

}