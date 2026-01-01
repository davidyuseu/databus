package sy.common.socket.entity;
import lombok.Data;

@Data
public class FrameType {
    //被拆分字节所在位置，目前仅支持单字节
    private int bytePos;
    //bit位从低bitStart到高bitEnd，其中bitEnd应该大于bitStart
    private int bitStart;
    private int bitEnd;
    //构成的新byte的十六进制值
    private String newByteValue;
}
