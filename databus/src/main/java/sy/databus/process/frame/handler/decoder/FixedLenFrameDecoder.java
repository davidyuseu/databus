package sy.databus.process.frame.handler.decoder;

import lombok.Setter;
import sy.databus.process.Console;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import sy.databus.process.frame.handler.Handler;

import static io.netty.util.internal.ObjectUtil.checkPositive;
import static sy.databus.process.frame.handler.Handler.Category.HANDLER_FRAME;

@Handler(
    category = HANDLER_FRAME,
    group = "decoder",
    name = "定长搜帧"
)
public class FixedLenFrameDecoder extends AbstractLockFrameTrans {
    // 非动态属性，搜帧类一般不适用动态属性，要避免缓冲区发生混乱
    @Getter @Setter
    @Console(config = Console.Config.STATIC)
    private int frameLen = 1;

    public FixedLenFrameDecoder() {} //AbstractHandler的子类必须拥有无参构造器，否则影响其反序列化

    public FixedLenFrameDecoder(int frameLen) {
        checkPositive(frameLen, "frameLen");
        this.frameLen = frameLen;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void boot() {
        super.boot();
        checkPositive(frameLen, "frameLen");
    }

    @Override
    protected void decode(ByteBuf cumulation)  {
        ByteBuf decoded = lockFrame(cumulation);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    protected ByteBuf lockFrame(ByteBuf cumulation)  {
        return cumulation.readableBytes() < this.frameLen
                ? null
                : cumulation.readRetainedSlice(this.frameLen);
    }
}
