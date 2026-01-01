package sy.databus.process.frame;

import lombok.extern.log4j.Log4j2;
import sy.databus.entity.signal.START;
import sy.databus.process.AbstractMessageProcessor;

import java.util.HashMap;

@Log4j2
public abstract class AbstractMessageProducer<T> extends AbstractMessageProcessor<T> {

    protected abstract void produce();

    @Override
    public void initialize() {
        super.initialize();
//        /** 在produce()方法中使用fireNext方法即可将生产出的消息往后转发*/
//        setNextHandler(tailHandler);
        // 生产者默认异步处理，响应START信号，将任务提交到适配的生产者执行器中
        appendSlot(START.class, signal -> {
            executor.execute(AbstractMessageProducer.this::produce);
            return true;
        });
    }
}
