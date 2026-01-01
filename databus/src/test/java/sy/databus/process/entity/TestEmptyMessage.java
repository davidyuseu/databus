package sy.databus.process.entity;

import io.netty.buffer.ByteBuf;
import sy.databus.entity.message.IMessage;
import sy.databus.entity.message.ByteBufMessage;
import sy.databus.entity.message.IMsgWithMetadata;

public class TestEmptyMessage extends ByteBufMessage {

    public TestEmptyMessage() {
    }


    @Override
    public IMessage copy() {
        return new TestEmptyMessage();
    }

    @Override
    public IMsgWithMetadata<ByteBuf> copyOnlyMetadata() {
        return null;
    }

    @Override
    public IMessage retainedDuplicate() {
        return null;
    }

    @Override
    public IMessage retain() {
        return null;
    }

    @Override
    public void release() {

    }

    @Override
    public long dataCapacity() {
        return 0;
    }

    @Override
    public void clear() {

    }
}
