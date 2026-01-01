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
public class BitsLengthSlider extends AbstractTransHandler<IMessage<ByteBuf>> {

    @Getter @Setter
    @Console(config = STATIC, display = "切片起始位")
    private int slicingStartPos;

    @Getter @Setter
    @Console(config = STATIC, display = "byte的位置")
    private int bytePos;

    @Getter @Setter
    @Console(config = STATIC, display = "bit结束位")
    private int bitEndPos;

    @Getter @Setter
    @Console(config = STATIC, display = "bit起始位")
    private int bitStartPos;

    @Override
    public void initialize() {

    }

    @Override
    public void handle(IMessage<ByteBuf> msg) throws Exception {
         ByteBuf buf = msg.getData();
         byte bt = buf.getByte(bytePos);
         int length = (bt >> bitStartPos) & (0xFF >> (8 - (bitEndPos - bitStartPos + 1)));
         if (length == 0) {
             msg.release();
             return;
         }

         length *= 32;

         if (length < 0 || slicingStartPos + length >= buf.capacity()) {
            log.error("切片长度为0，或切片长度越界！");
            msg.release();
            return;
         }
         msg.setData(buf.slice(slicingStartPos, length));
         fireNext(msg);
    }
}
