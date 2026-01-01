package sy.databus.entity.message;

import sy.databus.entity.message.metadata.IMetadata;
import sy.databus.entity.IEvent;
import sy.databus.process.frame.MessageSeriesProcessor;

public interface IMessage<T> extends IEvent {

    /** 是否需要拷贝消息
     * 主要在{@link MessageSeriesProcessor.Copier#handle(IMessage)}
     * 中使用，判别当前入站的数据是否需要拷贝。
     * */
    boolean getFutureCopy();
    /**
     * 主要在{@link MessageSeriesProcessor.OutBoundHandler}
     * 中使用，旨在当前消息传递到的下级综合处理器不是最终处理器时，标识该消息需要拷贝。
     * */
    void setFutureCopy(boolean futureCopy);

    /** 获取报文 */
    T getData();

    /** 设置报文*/
    void setData(T data);

    /**
     * 拷贝报文，包括元数据Metadata + 有效数据data
     * 主要在{@link MessageSeriesProcessor.Copier#handle(IMessage)}
     * 中使用，旨在当下级综合处理器存在多个时拷贝报文和元信息。
     * 1.创建新的Message
     * 2.对报文ByteBuf进行copy()
     * 3.同时对元信息clone() {@link IMetadata#clone()}
     *
     * ps:
     *  a. 不拷贝与框架强相关的非元数据的元信息（如futureCopy变量等）
     *  b. 不拷贝支撑数组，因为其仅在需要序列化时重新序列化
     * */
    IMessage<T> copy();

    /**
     * 1.对报文ByteBuf进行retainedDuplicate()
     * 2.不再对元信息中的支撑数组进行retainedDuplicate()，元数据的支撑数组交由用户自行维护
     * */
    IMessage retainedDuplicate();

    /**
     * 主要在{@link MessageSeriesProcessor.OutBoundHandler#handle(IMessage)}
     * 中使用，可使得{@link MessageSeriesProcessor#preHandlers}中处理的消息都是副本。
     * 1.对报文ByteBuf进行retain()
     * 2.不再对元信息中的支撑数组进行retainBackArray()，元数据的支撑数组交由用户自行维护
     * */
    IMessage retain();

/*
    // 对报文ByteBuf进行duplicate()，主要在OutBoundHandler中使用，使得prehandler中的msg都是副本
    IMessage duplicate();
*/

    /**
     * 1.对报文ByteBuf进行release()
     * 2.不再对元数据中的支撑数组进行release()，元数据的支撑数组交由用户自行维护
     * 3.其余成员的生命周期交由GC管理
     * */
    void release();

    /**
     * 返回报文ByteBuf的capacity
     * */
    long dataCapacity();

    void clear();
}
