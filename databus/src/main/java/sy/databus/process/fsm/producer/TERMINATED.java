package sy.databus.process.fsm.producer;


import static sy.databus.process.fsm.producer.State.ActionType.OPEN;

public class TERMINATED  extends State {

    public TERMINATED(Switch toOpen){
        super("TERMINATED");
        addAction(OPEN, toOpen);
    }

}
