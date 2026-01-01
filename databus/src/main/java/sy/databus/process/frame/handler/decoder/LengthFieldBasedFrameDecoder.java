package sy.databus.process.frame.handler.decoder;

import io.netty.buffer.ByteBuf;

import lombok.Getter;
import lombok.Setter;
import sy.databus.process.Console;
import sy.databus.process.frame.handler.Handler;

import static io.netty.util.internal.ObjectUtil.*;
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;
import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.frame.handler.Handler.Category.HANDLER_FRAME;

@Handler(
        category = HANDLER_FRAME,
        group = "decoder",
        name = "长度域变长搜帧"
)
public class LengthFieldBasedFrameDecoder extends AbstractLockFrameTrans {

    // 大小端设置 0 小端 1 大端 #-枚举
    protected int byteOrder = 0;
    @Getter @Setter
    @Console(config = STATIC, display = "最大帧长")
    protected int maxFrameLength = 65535;
    @Getter @Setter
    @Console(config = STATIC, display = "长度域位置")
    protected int lengthFieldOffset;
    @Getter @Setter
    @Console(config = STATIC, display = "长度域长度")
    protected int lengthFieldLength = 1;
    protected int lengthFieldEndOffset;
    @Getter @Setter
    @Console(config = STATIC, display = "长度调整量")
    protected int lengthAdjustment;
    @Getter @Setter
    @Console(config = STATIC, display = "截取起始位置")
    protected int initialBytesToStrip;
    protected boolean failFast;
    protected boolean discardingTooLongFrame;
    protected long tooLongFrameLength;
    protected long bytesToDiscard;

    public LengthFieldBasedFrameDecoder() {}

    public LengthFieldBasedFrameDecoder(
            int maxFrameLength,
            int lengthFieldOffset, int lengthFieldLength) {
        this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0);
    }

    public LengthFieldBasedFrameDecoder(
            int maxFrameLength,
            int lengthFieldOffset, int lengthFieldLength,
            int lengthAdjustment, int initialBytesToStrip) {
        this(
                maxFrameLength,
                lengthFieldOffset, lengthFieldLength, lengthAdjustment,
                initialBytesToStrip, true);
    }

    public LengthFieldBasedFrameDecoder(
            int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
            int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        this(
                0, maxFrameLength, lengthFieldOffset, lengthFieldLength,
                lengthAdjustment, initialBytesToStrip, failFast);
    }

    public LengthFieldBasedFrameDecoder(
            int byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
            int lengthAdjustment, int initialBytesToStrip, boolean failFast) {

        this.byteOrder = byteOrder; // #-枚举

        checkPositive(maxFrameLength, "maxFrameLength");

        checkPositiveOrZero(lengthFieldOffset, "lengthFieldOffset");

        checkPositiveOrZero(initialBytesToStrip, "initialBytesToStrip");

        if (lengthFieldOffset > maxFrameLength - lengthFieldLength) {
            throw new IllegalArgumentException(
                    "maxFrameLength (" + maxFrameLength + ") " +
                            "must be equal to or greater than " +
                            "lengthFieldOffset (" + lengthFieldOffset + ") + " +
                            "lengthFieldLength (" + lengthFieldLength + ").");
        }

        this.maxFrameLength = maxFrameLength;
        this.lengthFieldOffset = lengthFieldOffset;
        this.lengthFieldLength = lengthFieldLength;
        this.lengthAdjustment = lengthAdjustment;
        this.lengthFieldEndOffset = lengthFieldOffset + lengthFieldLength;
        this.initialBytesToStrip = initialBytesToStrip;
        this.failFast = failFast;
    }

    @Override
    public void initialize() {

    }

    @Override
    protected void decode(ByteBuf cumulation) throws Exception {
        ByteBuf decoded = lockFrame(cumulation);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    protected void discardingTooLongFrame(ByteBuf in) {
        long bytesToDiscard = this.bytesToDiscard;
        int localBytesToDiscard = (int) Math.min(bytesToDiscard, in.readableBytes());
        in.skipBytes(localBytesToDiscard);
        bytesToDiscard -= localBytesToDiscard;
        this.bytesToDiscard = bytesToDiscard;

        failIfNecessary(false);
    }

    protected static void failOnNegativeLengthField(ByteBuf in, long frameLength, int lengthFieldEndOffset) {
        in.skipBytes(lengthFieldEndOffset);
        throw new CorruptedFrameException(
                "negative pre-adjustment length field: " + frameLength);
    }


    protected static void failOnFrameLengthLessThanLengthFieldEndOffset(ByteBuf in,
                                                                      long frameLength,
                                                                      int lengthFieldEndOffset) {
        in.skipBytes(lengthFieldEndOffset);
        throw new CorruptedFrameException(
                "Adjusted frame length (" + frameLength + ") is less " +
                        "than lengthFieldEndOffset: " + lengthFieldEndOffset);
    }

    protected void exceededFrameLength(ByteBuf in, long frameLength) {
        long discard = frameLength - in.readableBytes();
        tooLongFrameLength = frameLength;

        if (discard < 0) {
            // buffer contains more bytes then the frameLength so we can discard all now
            in.skipBytes((int) frameLength);
        } else {
            // Enter the discard mode and discard everything received so far.
            discardingTooLongFrame = true;
            bytesToDiscard = discard;
            in.skipBytes(in.readableBytes());
        }
        failIfNecessary(true);
    }

    protected static void failOnFrameLengthLessThanInitialBytesToStrip(ByteBuf in,
                                                                     long frameLength,
                                                                     int initialBytesToStrip) {
        in.skipBytes((int) frameLength);
        throw new CorruptedFrameException(
                "Adjusted frame length (" + frameLength + ") is less " +
                        "than initialBytesToStrip: " + initialBytesToStrip);
    }

    protected ByteBuf lockFrame(ByteBuf cumulation) throws Exception {
        if (discardingTooLongFrame) {
            discardingTooLongFrame(cumulation);
        }

        if (cumulation.readableBytes() < lengthFieldEndOffset) {
            return null; // 在丢弃模式下，如果没有丢干净，必然进这里
        }

        int actualLengthFieldOffset = cumulation.readerIndex() + lengthFieldOffset;
        long frameLength = getUnadjustedFrameLength(cumulation, actualLengthFieldOffset, lengthFieldLength, byteOrder);

        if (frameLength < 0) {
            failOnNegativeLengthField(cumulation, frameLength, lengthFieldEndOffset);
        }

        frameLength += lengthAdjustment + lengthFieldEndOffset; // 得到整包长度

        if (frameLength < lengthFieldEndOffset) { // 整包长度不应该比包头长度（lengthFieldEndOffset）还短
            failOnFrameLengthLessThanLengthFieldEndOffset(cumulation, frameLength, lengthFieldEndOffset);
        }

        if (frameLength > maxFrameLength) {
            exceededFrameLength(cumulation, frameLength);
            return null;
        }

        // 不会溢出，因为一定小于maxFrameLength
        int frameLengthInt = (int) frameLength;
        if (cumulation.readableBytes() < frameLengthInt) {
            return null;
        }

        if (initialBytesToStrip > frameLengthInt) {
            failOnFrameLengthLessThanInitialBytesToStrip(cumulation, frameLength, initialBytesToStrip);
        }
        cumulation.skipBytes(initialBytesToStrip);

        // 截取帧
        int readerIndex = cumulation.readerIndex();
        int actualFrameLength = frameLengthInt - initialBytesToStrip;
        ByteBuf frame = extractFrame(cumulation, readerIndex, actualFrameLength);
        cumulation.readerIndex(readerIndex + actualFrameLength);
        return frame;
    }

    protected long getUnadjustedFrameLength(ByteBuf buf, int offset, int length, int order) {
        long frameLength;
        switch (length) {
            case 1:
                frameLength = buf.getUnsignedByte(offset);
                break;
            case 2:
                frameLength = order == 0 ? buf.getUnsignedShortLE(offset) : buf.getUnsignedShort(offset);
                break;
            case 3:
                frameLength = order == 0 ? buf.getUnsignedMediumLE(offset) : buf.getUnsignedMedium(offset);
                break;
            case 4:
                frameLength = order == 0 ? buf.getUnsignedIntLE(offset) : buf.getUnsignedInt(offset);
                break;
            case 8:
                frameLength = order == 0 ? buf.getLongLE(offset) : buf.getLong(offset);
                break;
            default:
                throw new DecoderException(
                        "unsupported lengthFieldLength: " + lengthFieldLength + " (expected: 1, 2, 3, 4, or 8)");
        }
        return frameLength;
    }

    private void failIfNecessary(boolean firstDetectionOfTooLongFrame) {
        if (bytesToDiscard == 0) {
            // Reset to the initial state and tell the handlers that
            // the frame was too large.
            long tooLongFrameLength = this.tooLongFrameLength;
            this.tooLongFrameLength = 0;
            discardingTooLongFrame = false;
            if (!failFast || firstDetectionOfTooLongFrame) {
                fail(tooLongFrameLength);
            }
        } else {
            // Keep discarding and notify handlers if necessary.
            if (failFast && firstDetectionOfTooLongFrame) {
                fail(tooLongFrameLength);
            }
        }
    }

    /**
     * Extract the sub-region of the specified buffer.
     */
    protected ByteBuf extractFrame(ByteBuf buffer, int index, int length) {
        return buffer.retainedSlice(index, length);
    }

    private void fail(long frameLength) {
        if (frameLength > 0) {
            throw new TooLongFrameException(
                    "Adjusted frame length exceeds " + maxFrameLength +
                            ": " + frameLength + " - discarded");
        } else {
            throw new TooLongFrameException(
                    "Adjusted frame length exceeds " + maxFrameLength +
                            " - discarding");
        }
    }
}
