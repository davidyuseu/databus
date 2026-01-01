package sy.databus.process.frame.handler.decoder;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import sy.common.util.SByteUtil;
import sy.databus.process.Console;
import sy.databus.process.frame.handler.Handler;

import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.frame.handler.Handler.Category.HANDLER_FRAME;

@Handler(
        category = HANDLER_FRAME,
        group = "decoder",
        name = "固定帧头变长搜帧"
)
public class HeaderLengthFieldBasedFrameDecoder extends LengthFieldBasedFrameDecoder {

    @Getter @Setter
    @Console(config = STATIC, display = "帧头")
    private ByteBuf header = null;

    private boolean foundHeader = false;

    public HeaderLengthFieldBasedFrameDecoder() {}

    public HeaderLengthFieldBasedFrameDecoder(
            int maxFrameLength,
            int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0);
    }

    @Override
    protected ByteBuf lockFrame(ByteBuf cumulation) throws Exception {
        if (discardingTooLongFrame) {
            discardingTooLongFrame(cumulation);
        }

        if (cumulation.readableBytes() < lengthFieldEndOffset) {
            return null; // 在丢弃模式下，如果没有丢干净（cumulation.readableBytes() == 0），必然进这里
        }
        // 非丢弃模式
        if(!foundHeader){
            int headerIndex = SByteUtil.indexOf(cumulation, header);
            if(headerIndex < 0) {
                cumulation.readerIndex(cumulation.writerIndex());
                return null;
            }else { // find the header
                cumulation.readerIndex(headerIndex); // 指针移至header索引位置
                foundHeader = true;
            }
        }

        int actualLengthFieldOffset = cumulation.readerIndex() + lengthFieldOffset;
        long frameLength = getUnadjustedFrameLength(cumulation, actualLengthFieldOffset, lengthFieldLength, byteOrder);

        if (frameLength < 0) {
            foundHeader = false;
            failOnNegativeLengthField(cumulation, frameLength, lengthFieldEndOffset);
        }

        frameLength += lengthAdjustment + lengthFieldEndOffset; // 得到整包长度

        if (frameLength < lengthFieldEndOffset) { // 整包长度不应该比包头长度（lengthFieldEndOffset）还短
            foundHeader = false;
            failOnFrameLengthLessThanLengthFieldEndOffset(cumulation, frameLength, lengthFieldEndOffset);
        }

        if (frameLength > maxFrameLength) {
            foundHeader = false;
            exceededFrameLength(cumulation, frameLength);
            return null;
        }

        // 不会溢出，因为一定小于maxFrameLength
        int frameLengthInt = (int) frameLength;
        if (cumulation.readableBytes() < frameLengthInt) {
            return null;
        }

        if (initialBytesToStrip > frameLengthInt) {
            foundHeader = false;
            failOnFrameLengthLessThanInitialBytesToStrip(cumulation, frameLength, initialBytesToStrip);
        }
        cumulation.skipBytes(initialBytesToStrip);

        // 截取帧
        int readerIndex = cumulation.readerIndex();
        int actualFrameLength = frameLengthInt - initialBytesToStrip;
        ByteBuf frame = extractFrame(cumulation, readerIndex, actualFrameLength);
        cumulation.readerIndex(readerIndex + actualFrameLength);
        foundHeader = false;
        return frame;
    }
}
