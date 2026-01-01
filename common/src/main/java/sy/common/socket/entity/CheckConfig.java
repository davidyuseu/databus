package sy.common.socket.entity;

import lombok.Data;

/**
 * 校验配置类
 */
@Data
public class CheckConfig {
    public static final int BIG_INDIAN = 1;
    public static final int LITTLE_INDIAN = 2;
    // 和校验
    public static String TYPE_SUM = "sum";
    // 异或校验
    public static String TYPE_XOR = "xor";
    // crc校验
    public static String TYPE_CRC = "crc";
    // 校验类型
    private String checkType;
    private int checkStartPos, checkLength, resultSize;
    // 结果的字节序，大小端
    private int resultByteOrder;
    private int resultPos;
}
