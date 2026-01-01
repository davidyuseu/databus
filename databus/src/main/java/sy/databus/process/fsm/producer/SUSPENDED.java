package sy.databus.process.fsm.producer;


import static sy.databus.process.fsm.producer.State.ActionType.*;

public class SUSPENDED extends State {

    public SUSPENDED(Switch toOpen, Switch toClose, Switch toShutdown){
        super("SUSPENDED");
        addAction(OPEN, toOpen);
        addAction(CLOSE, toClose);
        addAction(SHUTDOWN, toShutdown);
    }

}