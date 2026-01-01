package sy.databus.process.frame.handler.common;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.databus.entity.message.IMessage;
import sy.databus.process.Console;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.frame.handler.Handler;

import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.frame.handler.Handler.Category.HANDLER_FRAME;

@Handler(category = HANDLER_FRAME)
@Log4j2
public class DataSlicer extends AbstractTransHandler<IMessage<ByteBuf>> {

    @Setter @Getter
    @Console(config = STATIC, display = "切片起始位")
    private int startPos;
    @Getter @Setter
    @Console(config = STATIC, display = "切片长度")
    private int dataLength;

    @Override
    public void initialize() {

    }

    @Override
    public void handle(IMessage<ByteBuf> msg) throws Exception {
        ByteBuf buf = msg.getData();
        if (dataLength < 0 || startPos + dataLength > buf.capacity()) {
            log.error("切片长度为0，或切片长度越界！");
            msg.release();
            return;
        }
        msg.setData(buf.slice(startPos, dataLength));
        fireNext(msg);
    }
}
