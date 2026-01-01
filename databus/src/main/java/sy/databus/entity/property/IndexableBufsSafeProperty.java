package sy.databus.entity.property;

import sy.common.util.SComponentUtils;
import sy.common.util.SStringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class IndexableBufsSafeProperty implements IProperty<Map<Integer, ByteBuf>>{
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexableBufsSafeProperty.class);

    Map<Integer, ByteBuf> value;

    @Override
    public synchronized Map<Integer, ByteBuf> getValue() {
        return value;
    }

    @Override
    public synchronized void setValue(Map<Integer, ByteBuf> byteBufs) throws Exception {
        checkValue(byteBufs);
        this.value = byteBufs;
    }

    private void checkValue(Map<Integer, ByteBuf> byteBufMap) throws Exception {
        SComponentUtils.nonNullMap(byteBufMap);
        for(Map.Entry<Integer, ByteBuf> entry : byteBufMap.entrySet()){
            if(!entry.getValue().hasArray()) {
                LOGGER.warn("{}","All the elements of IndexableBufsSafeProperty's ByteBuf array should be heap buffer!");
            }
            if(entry.getValue().capacity() != 1) {
                clearValue(byteBufMap);
                throw new PropertyException("The capacity of IndexableBufsSafeProperty's ByteBuf should be 1!");
            }
        }
    }

    private void clearValue(Map<Integer, ByteBuf> byteBufMap) {
        if(byteBufMap == null)
            return;
        for(Map.Entry<Integer, ByteBuf> entry : byteBufMap.entrySet()){
            ByteBuf tBuf = entry.getValue();
            if(tBuf.refCnt() > 0)
                tBuf.release(tBuf.refCnt());
        }
        byteBufMap.clear();
    }

    /**
     * Input format:
     *  Map<Integer, String> - 字节位置，十六进制字符(单个)
     *  {(0,"eb"), (1,"90"),(4,"01")} √
     * */
    @Override
    public synchronized <K> void setValueByInput(K input) throws Exception {
        Objects.requireNonNull(input);
        if(!(input instanceof Map))
            throw new PropertyException("The actual parameter of the method 'setValueByInput' must be of type Map");
        else {
            Map<Integer, String> mapInput = (Map<Integer, String>) input;
            Map<Integer, ByteBuf> tMap = new ConcurrentHashMap<>();
            for(Map.Entry<Integer, String> entry : mapInput.entrySet()){
                String hexStr = SStringUtil.preEditHexInputString(entry.getValue());
                ByteBuf buf = Unpooled.wrappedBuffer(StringUtil.decodeHexDump(hexStr));
                tMap.put(entry.getKey(), buf);
            }
            checkValue(tMap);
            clearValue(value);
            value = tMap;
        }
    }
}
