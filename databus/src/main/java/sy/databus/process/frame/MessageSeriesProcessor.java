package sy.databus.process.frame;

import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import sy.databus.entity.IEvent;
import sy.databus.entity.ProcessorId;
import sy.databus.entity.message.IMessage;
import sy.databus.global.ProcessorType;

import sy.databus.process.*;


import sy.databus.view.watch.MessageSeriesProcessorWatchPane;

import static sy.databus.process.Console.Config.DYNAMIC;
import static sy.databus.process.Console.Config.STATIC;

@Log4j2
@Processor(
        type = ProcessorType.FRAME,
        pane = MessageSeriesProcessorWatchPane.class
)
public class MessageSeriesProcessor<T> extends AbstractMessageProcessor<T> {

    public MessageSeriesProcessor() {
        /** 串行消息处理器必须拥有{@link sy.databus.process.AbstractMessageProcessor.Copier}*/
        setCopier(DEFAULT_COPIER);
    }

    public MessageSeriesProcessor(ProcessorId id) {
        //帧处理型综合处理器
        super(id);
        /** 串行消息处理器必须拥有{@link sy.databus.process.AbstractMessageProcessor.Copier}*/
        setCopier(DEFAULT_COPIER);
    }

    protected class ClosingHandler extends AbstractTransHandler<IMessage<T>> {
        ClosingHandler() {
            setParentProcessor(MessageSeriesProcessor.this);
        }

        @Override
        public void initialize() {
            this.name = "ClosingHandler";
            this.id = (byte) 0xff;
        }

        @Override
        public void handle(IMessage<T> msg) throws Exception {
            msg.release();
        }
    }

    @Getter @Setter
    @Console(config = STATIC, display = "异步处理")
    protected SimpleBooleanProperty asynchronousFlag = new SimpleBooleanProperty(asynchronous){
        @Override
        protected void invalidated() {
            super.invalidated();
            asynchronous = get();
        }
    };

    /**
     * 由于反序列化时调用无参构造方法来实例化，此后可能会有部分成员变量被初始化为null
     * 所以框架需要调用initialize()方法再对它们进行初始化
     * */
    @Override
    public void initialize(){
        super.initialize();

        installPreHandlers();

        installServiceHandlers();

    }

}
