package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.process.IRouter;

/**
 * 约定大于设计：
 *  所有的ISignal的非抽象子类必须是final的（其抽象子类用来分类信号）；
 *  目前认为信号是只读的，无需在传递到分支时拷贝；
 * */
public interface ISignal extends IEvent {

    interface IRoutingPattern {

        /** 根据信号处理的结果{@param sigState},决定如何路由处理该信号{@param signal}*/
        void route(IRouter<IEvent> proc, boolean sigState, ISignal signal) throws Exception;
    }

    IRouter<IEvent> getSource();

    /** 信号名称*/
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /** 路由模式（往下级传递信号的方式）*/
    void setRoutingPattern(IRoutingPattern router);
    IRoutingPattern getRoutingPattern();

    /** 是否强制同步传递*/
    boolean isMandatorySync();

    /**
     *  消息号
     *  ps：须确保唯一性，与{@link #isReconsumable()}的应用相关
     *  */
    int getNum();

    /** 是否允许重复响应*/
    boolean isReconsumable();
}
