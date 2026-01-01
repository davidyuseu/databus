package sy.databus.process.frame.handler.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import sy.common.util.SByteUtil;
import sy.databus.entity.message.IMessage;
import sy.databus.process.Console;
import sy.databus.process.ProcessorInitException;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.frame.handler.Handler;


/**
 * 按标识顺序拼接子帧，发生乱序或非标识帧则清空重置
 * */
import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.frame.handler.Handler.Category.HANDLER_FRAME;


@Handler(
        category = HANDLER_FRAME,
        name = "子帧缓存拼接器"
)
public class SubFrameSplicer extends AbstractTransHandler<IMessage<ByteBuf>> {

    @Setter @Getter
    @Console(config = STATIC, display = "缓存标识")
    private ByteBuf flags = null;
    private int flagIndex = 0;

    @Setter @Getter
    @Console(config = STATIC, display = "标识位置")
    private int flagPos = 0;

    private CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();

    public SubFrameSplicer() {}

    @SneakyThrows
    public SubFrameSplicer(String strFlags, int flagPos) {
        this.flags = SByteUtil.getBufByString(strFlags);
        this.flagPos = flagPos;
        if (flags.capacity() > 16)
            throw new ProcessorInitException("The size of flags exceed the max limit!");
    }

    @Override
    public void initialize() {

    }

    @Override
    public void handle(IMessage<ByteBuf> msg) throws Exception {
        ByteBuf buf = msg.getData();
        if (buf.getByte(flagPos) == flags.getByte(flagIndex)) {
            compositeByteBuf.addComponents(true, buf);
            flagIndex++;
            if (flagIndex >= flags.capacity()) {
                msg.setData(compositeByteBuf);
                fireNext(msg);
                flagIndex = 0;
                compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
            }
        } else {
            msg.release();
            if (compositeByteBuf.numComponents() > 0) {
                compositeByteBuf.removeComponents(0, flagIndex);
                compositeByteBuf.resetWriterIndex();
                compositeByteBuf.resetReaderIndex();
                flagIndex = 0;
            }
        }
    }
}
