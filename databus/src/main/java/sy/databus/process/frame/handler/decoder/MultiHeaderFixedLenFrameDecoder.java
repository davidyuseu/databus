package sy.databus.process.frame.handler.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.common.util.SByteUtil;
import sy.common.util.SStringUtil;
import sy.databus.process.Console;
import sy.databus.process.ProcessorInitException;
import sy.databus.process.frame.handler.Handler;

import java.util.ArrayList;
import java.util.List;

import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.frame.handler.Handler.Category.HANDLER_FRAME;

@Log4j2
@Handler(
        category = HANDLER_FRAME,
        group = "decoder",
        name = "多帧头定长搜帧"
)
public class MultiHeaderFixedLenFrameDecoder extends AbstractLockFrameTrans {
    @Getter @Setter
    @Console(config = STATIC, display = "多帧头")
    private String strHeaders = "";

    @Getter @Setter
    @Console(config = STATIC, display = "固定帧长")
    private int frameLength = 1;

    private boolean foundHeader = false;
    private int headerOffset = -1; // 帧头相对于readerIndex的偏移量

    private List<ByteBuf> headers = new ArrayList<>(8);

    //AbstractHandler的子类必须拥有无参构造器，否则影响其反序列化
    public MultiHeaderFixedLenFrameDecoder() {}

    @Override
    public void initialize() {
        name = "多帧头定长搜帧";
    }

    @Override
    protected void decode(ByteBuf cumulation) throws Exception {
        ByteBuf decoded = lockFrame(cumulation);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    private ByteBuf lockFrame(ByteBuf cumulation) throws Exception {
        if (!foundHeader) {
            for (var header : headers) {
               headerOffset = SByteUtil.indexOf(cumulation, header) - cumulation.readerIndex();
               if (headerOffset >= 0) {
                   break;
               }
            }
            if (headerOffset < 0) {
                cumulation.readerIndex(cumulation.writerIndex());
                return null;
            } else {
                foundHeader = true;
            }
        }
        if (cumulation.readableBytes() - headerOffset < frameLength) {
            return null;
        } else {
            cumulation.readerIndex(cumulation.readerIndex() + headerOffset);
            foundHeader = false;
            return cumulation.readRetainedSlice(frameLength);
        }
    }

    @Override
    public void boot() {
        super.boot();
        SByteUtil.releaseAndClearBufs(headers);
        if (!strHeaders.isEmpty()) {
            String[] strs = strHeaders.split(";");
            for(var str : strs) {
                if (!str.isEmpty()) {
                    try {
                        str = SStringUtil.preEditHexInputString(str);
                        byte[] bytes = StringUtil.decodeHexDump(str);
                        if (bytes.length > 0) {
                            var buf = Unpooled.wrappedBuffer(bytes);
                            headers.add(buf);
                        } else {
                            log.warn("{}","The length of byte array transferred by header is 0!");
                        }
                    } catch (Exception e) {
                        String errMsg = "There is an error input in headers!";
                        log.error("{}", errMsg);
                        throw new ProcessorInitException(errMsg);
                    }

                } else {
                    log.error("{}", "There is a header is empty!");
                }
            }
        }

    }
}
