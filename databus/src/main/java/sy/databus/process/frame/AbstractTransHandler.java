package sy.databus.process.frame;

import lombok.Getter;
import lombok.Setter;
import sy.databus.global.WorkMode;
import sy.databus.process.AbstractHandler;
import sy.databus.process.IDataTrans;
import sy.databus.process.IEventProc;

public abstract class AbstractTransHandler<T> extends AbstractHandler<T>
        implements IDataTrans<T>{
    @Getter
    @Setter
    protected volatile IEventProc<T> nextHandler; //引用下一个Handler(同一个IntegratedProcessor中的)
    /**
     * default implementation of {@link IDataTrans}
     * */
    @Override
    public void fireNext(T o) throws Exception{
        nextHandler.handle(o);
    }

}
