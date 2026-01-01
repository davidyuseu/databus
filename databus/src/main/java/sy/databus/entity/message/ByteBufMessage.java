package sy.databus.entity.message;

import sy.databus.entity.message.metadata.IMetadata;
import io.netty.buffer.ByteBuf;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;

public abstract class ByteBufMessage implements IMsgWithMetadata<ByteBuf> {

    protected boolean futureCopy = false;
        @Override
        public boolean getFutureCopy(){ return futureCopy; }
        @Override
        public void setFutureCopy(boolean futureCopy){
            this.futureCopy = futureCopy;
        }

    protected IMetadata metadata;
        @Override
        public IMetadata getMetadata(){ return metadata; }
        @Override
        public void setMetadata(IMetadata iMetadata){ this.metadata = iMetadata; }

    protected ByteBuf data;
        @Override
        public ByteBuf getData() {
            return data;
        }
        @Override
        public void setData(ByteBuf data) {
            this.data = data;
        }

    @Override
    public void appendDumpInfo(StringBuilder sb) {
        appendPrettyHexDump(sb, data);
    }

}
