package sy.databus.entity.property;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sy.common.util.SStringUtil;
import sy.databus.process.ProcessorInitException;

import java.util.Objects;

/**
 * ps: Strongly suggest to wrap heap array to use it.
 * pane is {@link sy.databus.view.controller.ConsecutiveBufController}
 * */
public class ConsecutiveBufSafeProperty implements IProperty<ByteBuf>{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsecutiveBufSafeProperty.class);

    ByteBuf value;

    public ConsecutiveBufSafeProperty(ByteBuf value) {
        this.value = value;
    }

    public ConsecutiveBufSafeProperty(String stringValue) {
        try {
            setValueByString(stringValue);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProcessorInitException("Failed to construct the obj of 'ConsecutiveBufSafeProperty'!");
        }
    }

    @Override
    public synchronized ByteBuf getValue() {
        return value;
    }

    @Override
    public synchronized void setValue(ByteBuf byteBuf) throws Exception{
        if(!byteBuf.hasArray()) {
            if (byteBuf.refCnt() > 0) {
                byteBuf.release(byteBuf.refCnt());
            }
            throw new PropertyException("you must wrap heap array to use 'ConsecutiveBufSafeProperty'!");
        }
        this.value = byteBuf;
    }

    /**
     * Input format:
     *  √ "eb900401"
     *  √ "eb 90 04 01"  (preEditHexInputString方法会去除空白字符)
     *      => byte[]{0xeb, 0x90, 0x04, 0x01}
     *
     *  × "0x90eb" 不识别"0x"
     *  × "eb,90" 或 "eb，90" 或 "eb;90" 不识别非[a-zA-Z0-9]的字符
     *  × "eb90e" 长度不为偶数
     *
     * */
    @Override
    public synchronized <K> void setValueByInput(K input) throws Exception{
        Objects.requireNonNull(input);
        if(!(input instanceof String))
            throw new PropertyException("The actual parameter of the method 'setValueByInput' must be of type String");
        else {
            String strInput = (String) input;
            setValueByString(strInput);
        }
    }

    private void setValueByString(String input) throws Exception{
        if (value != null) {
            if(value.refCnt() > 0)
                value.release(value.refCnt());
        }
        if(!input.isEmpty()) {
            input = SStringUtil.preEditHexInputString(input);
            byte[] bytes = StringUtil.decodeHexDump(input);
            if (bytes.length > 0) {
                value = Unpooled.wrappedBuffer(bytes);
            } else {
                LOGGER.warn("{}","The length of byte array transferred by input is 0!");
            }
        }else{
            LOGGER.warn("{}","The input String is empty!");
        }
    }

}
