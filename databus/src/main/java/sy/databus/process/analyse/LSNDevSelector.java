package sy.databus.process.analyse;

import io.netty.buffer.ByteBuf;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.databus.entity.IEvent;
import sy.databus.entity.ProcessorId;
import sy.databus.entity.message.IMessage;
import sy.databus.entity.property.RadioSelectObList;
import sy.databus.global.ProcessorType;
import sy.databus.process.*;
import sy.databus.view.watch.LSNDevSelectorWatchPane;

import static sy.databus.process.Console.Config.DYNAMIC;
import static sy.databus.process.Console.Config.STATIC;

@Log4j2
@Processor(
        type = ProcessorType.FRAME,
        pane = LSNDevSelectorWatchPane.class,
        coupledParents = {LSNFileReplayer.class}
)
public class LSNDevSelector extends AbstractMessageProcessor<ByteBuf> {

    @Setter @Getter
    @Console(config = STATIC, display = "输入设备")
    private RadioSelectObList<String> inDevs;

    public LSNDevSelector() {}

    public LSNDevSelector(ProcessorId id) {
        super(id);
    }

    @Override
    public void initialize() {
        super.initialize();

        asynchronous = false;

        installServiceHandlers();

        if (inDevs == null)
            inDevs = new RadioSelectObList<>(false, -1);
    }

    @Setter @Getter
    @Console(config = DYNAMIC, display = "工作")
    protected volatile boolean passable = true;

    @Getter @Setter
    @Console(config = STATIC, display = "异步处理")
    protected SimpleBooleanProperty asynchronousFlag = new SimpleBooleanProperty(false){
        @Override
        protected void invalidated() {
            super.invalidated();
            asynchronous = get();
        }
    };

    @Override
    public void handle(IEvent event) throws Exception {
        if (event instanceof IMessage msg) {
            try{
                if (!passable) {
                    msg.release();
                    return;
                }
                if (!asynchronous) {
                    inBoundedStatistic(msg);
                    fireNext(msg);
                } else {
                    // 异步执行
                    executor.execute(new InterruptHandler() {
                        @Override
                        public void terminated() {
                            msg.clear();
                        }

                        @Override
                        public void run() {
                            inBoundedStatistic(msg);
                            try {
                                fireNext(msg);
                            } catch (Exception e) {
                                log.error(e);
                                handleExceptionMsg(msg);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                log.error(e);
                handleExceptionMsg(msg);
            }
        } else {
            super.handle(event);
        }
    }

    @Override
    public void connectedAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        super.connectedAsInput(previousProcessor);
        if (previousProcessor instanceof LSNFileReplayer lsnReplayFileReader) {
            inDevs.setCandidateList(lsnReplayFileReader.getCandidateDevs());
            inDevs.setOccupiedIndexGroup(lsnReplayFileReader.getSelectedDevs());
        } else {
            log.error("{} has coupled parent processor!", getClass().getSimpleName());
        }
    }

    @Override
    public void detachedAsInput(AbstractIntegratedProcessor parentNode) {
        super.detachedAsInput(parentNode);
        inDevs.setCandidateList(null);
        inDevs.setOccupiedIndexGroup(null);
    }

}
