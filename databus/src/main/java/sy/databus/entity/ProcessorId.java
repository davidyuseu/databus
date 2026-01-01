package sy.databus.entity;

import sy.databus.global.ProcessorType;
import sy.databus.process.AbstractIntegratedProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * 除了测试用例，ProcessorId的创建与销毁都应该由{@link sy.databus.organize.ProcessorManager} 来维护。
 * */
public class ProcessorId {
    /** ProcessorId的编码长度 */
    private static final int CODEC_LEN = 8;

    /**
     * byte[] 2字节，标识当前席位号
     * 编码位置 {@value SEATNUM_CODEC_POS}
     * */
    private final short seatNum;
    private static final int SEATNUM_CODEC_POS = 0;
    public short getSeatNum() { return seatNum; }

    /**
     * byte 1字节，标识当前处理类型
     * 编码位置 {@value PROCTYPE_CODEC_POS}
     * */
    private final ProcessorType procType;
    private static final int PROCTYPE_CODEC_POS = 2;
    public ProcessorType getProcType() { return procType; }

    /**
     * byte[] 2字节，标识当前综合处理器号
     * 编码位置 {@value PROCNUM_CODEC_POS}
     * */
    private final short procNum;
    private static final int PROCNUM_CODEC_POS = 3;
    public short getProcNum() { return procNum; }

    @Getter
    private String pIdHexString;

    @Getter
    private long processorCode;

    @Getter @Setter
    private AbstractIntegratedProcessor owner = null;

    /**
     * constructor
    */
    public ProcessorId(short seatNum, ProcessorType procType, short procNum) {
        this.seatNum = seatNum;
        this.procType = procType;
        this.procNum = procNum;
        this.processorCode = codec(seatNum, procType, procNum);
        pIdToHexString();
    }

    /**
     *  use in {@link sy.databus.organize.ProcessorManager#allocateProcessorId(short, Class)}
     */
    public ProcessorId(short seatNum, ProcessorType procType, short procNum, long processorCode) {
        this.seatNum = seatNum;
        this.procType = procType;
        this.procNum = procNum;
        this.processorCode = processorCode;
        pIdToHexString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o instanceof ProcessorId) {
            ProcessorId tPID = (ProcessorId) o;
            if(this.seatNum == tPID.seatNum
                    && this.procType.getCode() == tPID.procType.getCode()
                    && this.procNum == tPID.procNum
            ){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        long var = processorCode;
        return Long.hashCode(var);
    }

    public long codec(){
        return codec(this.seatNum, this.procType, this.procNum);
    }

    public static long codec(short seatNum, ProcessorType procType, short procNum){
        ByteBuf buf = Unpooled.buffer(CODEC_LEN);
        buf.writeShortLE(seatNum);
        buf.writeByte(procType.getCode());
        buf.writeShortLE(procNum);
        long value = buf.getLong(0);
        buf.release();
        return value;
    }

    public static long codec(ProcessorId processorId){
        return codec(processorId.getSeatNum(), processorId.getProcType(), processorId.getProcNum());
    }

    public static ProcessorId decode(long code){
        ByteBuf buf = Unpooled.buffer(CODEC_LEN);
        buf.writeLong(code);
        short seatNum = buf.getShortLE(SEATNUM_CODEC_POS);
        byte procTypeCode = buf.getByte(PROCTYPE_CODEC_POS);
        short procNum = buf.getShortLE(PROCNUM_CODEC_POS);
        buf.release();
        return new ProcessorId(seatNum, ProcessorType.getValue(procTypeCode), procNum, code);
    }

    private void pIdToHexString(){
        if(pIdHexString != null && pIdHexString.length() == CODEC_LEN * 2) {
            return;
        }else {
            ByteBuf buf = Unpooled.buffer(CODEC_LEN);
            buf.writeShortLE(seatNum);
            buf.writeByte(procType.getCode());
            buf.writeShortLE(procNum);
            pIdHexString = StringUtil.toHexStringPadded(buf.array());
            buf.release();
        }
    }

    @Override
    public String toString() {
        pIdToHexString();
        return pIdHexString;
    }
}
