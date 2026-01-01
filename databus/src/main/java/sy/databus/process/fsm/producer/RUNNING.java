package sy.databus.process.fsm.producer;

import static sy.databus.process.fsm.producer.State.ActionType.*;

public class RUNNING extends State {

    public RUNNING(Switch toPark, Switch toClose, Switch toShutdown){
        super("RUNNING");
        addAction(PARK, toPark);
        addAction(CLOSE, toClose);
        //TODO 考虑将shutdown提交给ExecutorManager中统一同步处理
        addAction(SHUTDOWN, toShutdown);
    }
}