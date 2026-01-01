package sy.databus.entity.message;

import sy.databus.entity.message.metadata.IMetadata;

import java.util.List;

public interface IMsgWithMetadata<T> extends IMessage<T> {
    /** 获取元信息 */
    IMetadata getMetadata();
    /** 设置元信息*/
    void setMetadata(IMetadata iMetadata);

    /**
     * 只拷贝除data以外的元信息，
     * 主要在{@link sy.databus.process.frame.handler.decoder.AbstractLockFrameTrans#fireMessages(List, int)}
     * 中使用，旨在搜帧完成后重置各报文的元信息
     * 1.创建新的Message
     * 2.对元信息clone() {@link IMetadata#clone()}
     *
     * ps:
     *  a. 不拷贝与框架强相关的非元数据的元信息（如futureCopy变量等）
     *  b. 不拷贝支撑数组，因为其仅在需要序列化时重新序列化
     * */
    IMsgWithMetadata copyOnlyMetadata();
}
