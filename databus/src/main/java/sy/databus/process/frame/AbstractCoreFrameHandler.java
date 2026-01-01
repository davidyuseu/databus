package sy.databus.process.frame;

import lombok.Getter;
import lombok.Setter;
import sy.databus.entity.message.IMessage;
import sy.databus.process.*;

/**
 * 核心处理器
 * 一个综合处理器中只能添加一个核心处理器
 * */
public abstract class AbstractCoreFrameHandler<T> extends AbstractTransHandler<IMessage<T>> {

    @Getter
    @Setter
    protected volatile IEventProc<IMessage<T>> nextHandler; //引用下一个Handler(同一个IntegratedProcessor中的)

    /**
     * default implementation of {@link IDataTrans}
     * */
    @Override
    public void fireNext(IMessage<T> msg) throws Exception{
        nextHandler.handle(msg);
    }

    @Override
    public void handle(IMessage<T> msg) throws Exception{
        coreHandle(preHandle(msg));
    }

    /** 核心处理，须被子类继承重写 */
    protected abstract void coreHandle(IMessage<T> msg) throws Exception;

    private final IMessage<T> preHandle(IMessage<T> msg) {
        if(msg.getFutureCopy()) {
            /**
             * 由于在{@link MessageSeriesProcessor.OutBoundHandler}
             * 中出站时调用了msg.retain()，创建了“副本”（增加了引用计数）
             * 所以这里复制完成前需要release()一次，销毁副本
             * */
            msg.release();
            IMessage<T> newMsg = msg.copy();
            return newMsg;
        }else{
            return msg;
        }
    }

}
