package sy.databus.process.frame.handler.filter;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ObjectUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.common.util.SByteUtil;
import sy.databus.entity.message.IMessage;
import sy.databus.process.Console;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.frame.handler.Handler;

import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.frame.handler.Handler.Category.HANDLER_FRAME;

@Handler(
        category = HANDLER_FRAME,
        group = "filter"
)
@Log4j2
public class HeaderAndLengthFilter extends AbstractTransHandler<IMessage<ByteBuf>> {

    @Setter @Getter
    @Console(config = STATIC, display = "判帧头")
    private ByteBuf header = null;
    @Getter @Setter
    @Console(config = STATIC, display = "判帧长")
    private int frameLength = 0;

    public HeaderAndLengthFilter() {}

    @SneakyThrows
    public HeaderAndLengthFilter(String header, int frameLength) {
        this.header = SByteUtil.getBufByString(header);
        this.frameLength = frameLength;
    }

    @Override
    public void initialize() {
        name = "帧头帧长过滤";
    }

    @Override
    public void handle(IMessage<ByteBuf> msg) throws Exception {
        ByteBuf msgBuf = msg.getData();
        if (msgBuf.capacity() < header.capacity()) {
            msg.release();
            //#- log
            log.warn("接收帧长小于判别帧头！");
            return;
        }

        if (frameLength == 0 || msgBuf.capacity() == frameLength) { // 先判帧长
            int index = 0;
            while (index < header.capacity()) {
                if (header.getByte(index) != msgBuf.getByte(index)) {
                    msg.release();
                    return;
                }
                ++index;
            }
            fireNext(msg);
        } else {
            msg.release();
        }
    }

    @Override
    public void boot() {
        super.boot();
        ObjectUtil.checkPositiveOrZero(frameLength, "frameLength");
    }
}
