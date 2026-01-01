package sy.databus.entity.message;

import io.netty.buffer.ByteBuf;
import sy.databus.entity.message.metadata.IMetadata;


public class EfficientMessage extends ByteBufMessage {

    // 以元数据和有效数据创建消息
    public EfficientMessage(IMetadata metadata, ByteBuf data) {
        this.metadata = metadata;
        this.data = data;
    }

    // 以元数据创建消息，有效数据为null
    public EfficientMessage(IMetadata metadata){
        this(metadata, null);
    }

    @Override
    public IMessage copy() {
        return new EfficientMessage(
                this.metadata.clone(),
                this.data.copy()
        );
    }

    /** 只拷贝元数据*/
    @Override
    public IMsgWithMetadata<ByteBuf> copyOnlyMetadata() {
        return new EfficientMessage(
                this.metadata.clone()
        );
    }

    @Override
    public IMsgWithMetadata<ByteBuf> retainedDuplicate() {
        return new EfficientMessage(
                this.metadata,
                this.data.retainedDuplicate()
        );
    }

    @Override
    public IMsgWithMetadata<ByteBuf> retain() {
        data.retain();
        /*
        * 元信息的支撑数组交由客户端自行维护（确保同栈创建、同栈释放）
        * */
//        metadata.retainBackArray();
        return this;
    }

    @Override
    public void release() {

        data.release();
        /*
         * 元信息的支撑数组交由客户端自行维护（确保同栈创建、同栈释放）
         * */
//        metadata.releaseBackArray();
    }

    @Override
    public long dataCapacity() {
        return data.writerIndex();
    }

    @Override
    public void clear() {
        if(data.refCnt() > 0)
            data.release(data.refCnt());
    }


}
