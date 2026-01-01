package sy.databus.process.frame.handler.decoder;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sy.databus.entity.message.IMsgWithMetadata;
import sy.databus.process.frame.AbstractTransHandler;

import java.util.ArrayList;
import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkPositive;
import static java.lang.Integer.MAX_VALUE;


public abstract class AbstractLockFrameTrans extends AbstractTransHandler<IMsgWithMetadata<ByteBuf>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLockFrameTrans.class);

    // 存放搜出的帧
    protected List<ByteBuf> out = new ArrayList<>(16);

    /**
     * 通过使用内存副本将入帧{@link ByteBuf} 合并为一个缓冲区 {@link ByteBuf} 来缓存入帧{@link ByteBuf}。
     */
    public static final Cumulator MERGE_CUMULATOR = new Cumulator() {
        @Override
        public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
            if (!cumulation.isReadable() && in.isContiguous()) {
                // 如果cumulation为空且输入缓冲区连续，则直接使用
                cumulation.release();
                return in;
            }
            try {
                final int required = in.readableBytes();
                if (required > cumulation.maxWritableBytes() ||
                        (required > cumulation.maxFastWritableBytes() && cumulation.refCnt() > 1) ||
                        cumulation.isReadOnly()) {
                    // 在以下条件扩容:
                    // - 缓冲区无法通过discardSomeReadBytes()来调整大小
                    // - 缓冲区的引用计数大于1，如用户使用了切片后增加了引用计数
                    return expandCumulation(alloc, cumulation, in);
                }
                cumulation.writeBytes(in, in.readerIndex(), required);
                in.readerIndex(in.writerIndex());
                return cumulation;
            } finally {
                // 必须在所有情况下都释放，否则如果 writeBytes(...) 抛出任何异常（例如因为 OutOfMemoryError），
                // 则可能会产生泄漏
                in.release();
            }
        }
    };

    ByteBuf cumulation;
    private Cumulator cumulator = MERGE_CUMULATOR;
    private boolean singleDecode;
    private boolean first;


    private int discardAfterReads = 16;
    private int numReads;

    protected AbstractLockFrameTrans() {
        /*ensureNotSharable();*/
    }

    /**
     * 如果设置，则每个 {@link #handle(IMsgWithMetadata) 调用只解码一条消息。
     * 如果需要进行一些协议升级并希望确保没有任何混淆，这可能很有用。
     * 默认为 {@code false}，因为会影响性能。
     */
    public void setSingleDecode(boolean singleDecode) {
        this.singleDecode = singleDecode;
    }

    /**
     * 如果 {@code true} 则在每个 {@link #handle(IMsgWithMetadata)} 调用中只解码一条消息。
     * 默认为 {@code false}，因为会影响性能。
     */
    public boolean isSingleDecode() {
        return singleDecode;
    }

    /**
     * 设置 {@link Cumulator} ，用于缓存接收到的 {@link ByteBuf}。
     */
    public void setCumulator(Cumulator cumulator) {
        this.cumulator = ObjectUtil.checkNotNull(cumulator, "cumulator");
    }

    /**
     * 设置调用 {@link ByteBuf#discardSomeReadBytes()} 之后的读取次数，从而释放内存。 默认值为 {@link #discardAfterReads}。
     */
    public void setDiscardAfterReads(int discardAfterReads) {
        checkPositive(discardAfterReads, "discardAfterReads");
        this.discardAfterReads = discardAfterReads;
    }

    /**
     * 返回此解码器内部累积缓冲区中的实际可读字节数。
     * 通常不需要依赖此值来编写解码器。
     */
    protected int actualReadableBytes() {
        return internalBuffer().readableBytes();
    }

    /**
     * 返回此解码器的内部累积缓冲区。
     * 通常不需要直接访问内部缓冲区来编写解码器。
     */
    protected ByteBuf internalBuffer() {
        if (cumulation != null) {
            return cumulation;
        } else {
            return Unpooled.EMPTY_BUFFER;
        }
    }

    @Override
    public void handle(IMsgWithMetadata<ByteBuf> msg) throws Exception {
            try {
                first = cumulation == null;
                /** 缓冲区*/
                cumulation = cumulator.cumulate(ByteBufAllocator.DEFAULT,
                        first ? Unpooled.EMPTY_BUFFER : cumulation, msg.getData() /** 入缓冲区字节数组*/);
                callDecode(cumulation, msg);
            } catch (DecoderException e) {
                throw e;
            } catch (Exception e) {
                throw new DecoderException(e);
            } finally {
                try {
                    if (cumulation != null && !cumulation.isReadable()) {
                        numReads = 0;
                        cumulation.release();
                        cumulation = null;
                    } else if (++numReads >= discardAfterReads) {
                        // 尝试丢弃一些已读字节，防止OOME.
                        numReads = 0;
                        discardSomeReadBytes();
                    }

                    int size = out.size();
                    fireLastMessages(out, size, msg);
                } finally {
                    out.clear();
                }
            }
    }

    /**
     * 将搜帧结果队列中的各消息送往下一个Handler，非最后一次解码的帧都应该复制当前元信息
     */
    private void fireMessages(List<ByteBuf> msgs, int numElements, IMsgWithMetadata<ByteBuf> inMsg) throws Exception {
        for (int i = 0; i < numElements; i++) {
            IMsgWithMetadata<ByteBuf> tMsg = inMsg.copyOnlyMetadata();// 拷贝元数据
            tMsg.setData(msgs.get(i)); // 重置数据
            fireNext(tMsg);
        }
    }

    /**
     * 将最后一次搜帧结果队列中的各消息送往下一个Handler
     */
    private void fireLastMessages(List<ByteBuf> msgs, int numElements, IMsgWithMetadata<ByteBuf> inMsg) throws Exception {
        for (int i = 0; i < numElements; i++) {
            IMsgWithMetadata<ByteBuf> tMsg;
            if(i == numElements - 1)
                tMsg = inMsg;
            else
                tMsg = inMsg.copyOnlyMetadata();// 拷贝元数据
            tMsg.setData(msgs.get(i)); // 重置数据
            fireNext(tMsg);
        }
    }


    protected final void discardSomeReadBytes() {
        if (cumulation != null && !first && cumulation.refCnt() == 1) {
            // 要考虑到用户可能使用 slice().retain() or duplicate().retain()来增加切片的引用计数
            cumulation.discardSomeReadBytes();
        }
    }

    /**
     * 解码读缓冲区
     * @param cumulation 读缓冲区
     */
    protected void callDecode(/** 堆积区*/ByteBuf cumulation, IMsgWithMetadata<ByteBuf> msg) {
        try {
            while (cumulation.isReadable()) { /** 堆积区读循环 */
                final int outSize = out.size();

                if (outSize > 0) {
                    fireMessages(out, outSize, msg);
                    out.clear();
                }

                int oldInputLength = cumulation.readableBytes();
                decode(cumulation);

                if (out.isEmpty()) {
                    /**
                     * 本次没有搜到帧，如“半包”现象。
                     * 注意！所有的搜帧方法中，
                     * 若搜到帧则将读指针移动至搜完帧的位置，
                     * 若丢弃帧则将读指针移动至丢弃帧的末尾，
                     * 否则不可以改变读写指针。
                     * */
                    if (oldInputLength == cumulation.readableBytes()) {
                        break; // 半包，跳出堆积区读循环
                    } else {
                        continue; // 弃帧，读指针发生移动，在下一次循环中从读指针处开始继续搜帧
                    }
                }

                if (oldInputLength == cumulation.readableBytes()) {
                    throw new DecoderException(
                            StringUtil.simpleClassName(getClass()) +
                                    ".decode() did not read anything but decoded a message.");
                }

                if (isSingleDecode()) {
                    break;
                }
            }
        } catch (DecoderException e) {
            throw e;
        } catch (Exception cause) {
            throw new DecoderException(cause);
        }
    }

    /**
     * 解码抽象方法，待具体实现
     *
     * @param cumulation            the {@link ByteBuf} from which to read data
     * @throws Exception    is thrown if an error occurs
     */
    protected abstract void decode(ByteBuf cumulation) throws Exception;


    static ByteBuf expandCumulation(ByteBufAllocator alloc, ByteBuf oldCumulation, ByteBuf in) {
        int oldBytes = oldCumulation.readableBytes();
        int newBytes = in.readableBytes();
        int totalBytes = oldBytes + newBytes;
        ByteBuf newCumulation = alloc.buffer(alloc.calculateNewCapacity(totalBytes, MAX_VALUE));
        ByteBuf toRelease = newCumulation;
        try {
            // 与调用writeBytes(...) 相比，避免了冗余检查和堆栈深度
            newCumulation.setBytes(0, oldCumulation, oldCumulation.readerIndex(), oldBytes)
                    .setBytes(oldBytes, in, in.readerIndex(), newBytes)
                    .writerIndex(totalBytes);
            in.readerIndex(in.writerIndex());
            toRelease = oldCumulation;
            return newCumulation;
        } finally {
            toRelease.release();
        }
    }

    /**
     * Cumulate {@link ByteBuf}s.
     */
    public interface Cumulator {
        /**
         * 缓存给定的 {@link ByteBuf} 并返回保存缓存字节的 {@link ByteBuf}。
         * 实现负责正确处理给定的 {@link ByteBuf} 等的生命周期
         * 如果 {@link ByteBuf} 被完全消耗，则调用 {@link ByteBuf#release()}。
         */
        ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in);
    }
}

