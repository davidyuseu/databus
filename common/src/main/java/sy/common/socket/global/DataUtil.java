package sy.common.socket.global;

import sy.common.socket.entity.CheckConfig;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.Identity;
import sy.common.socket.entity.UplinkDataHolder;
import sy.common.socket.entity.CheckConfig;
import sy.common.socket.entity.DataUnit;
import sy.common.socket.entity.Identity;
import sy.common.socket.entity.UplinkDataHolder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtil {

    public static final int HEX = 16;
    public static final int DEC = 10;
    public static final int LITTLE_ENDIAN = 1;
    public static final int BIG_ENDIAN = 2;

    public static enum DataOrder {
        LITTLE_ENDIAN(1),
        BIG_ENDIAN(2);


        private int code;

        private DataOrder(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
    /**
     * 组装数据
     * 加入head，加入标识位，加入校验
     *
     * @return 加入校验后的dataunit
     */
    public static DataUnit assembleDataUnit(UplinkDataHolder uplinkDataHolder) {
        DataUnit dataUnit = new DataUnit();
        dataUnit.bytes = new byte[uplinkDataHolder.getLength()];
        joinHead(dataUnit, uplinkDataHolder.getHead());
        joinIdentities(dataUnit, uplinkDataHolder.getIdentities());
        return dataUnit;
    }

    private static void joinHead(DataUnit unit, String head) {
        head = replaceBlank(head);
        for (int i = 0; i < head.length(); i += 2) {
            unit.bytes[i / 2] = (byte) Integer.parseInt(head.substring(i, i + 2), 16);
        }
    }

    /**
     * 删除字符串中空白字符
     *
     * @return 删除空白字符后的字符串
     */
    public static String replaceBlank(String str){
        String dest = "";
        if(str!=null){
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    /**
     *  将指定十六进制字符串cmd/cmdValue转换成十六机制byte数组，然后放入指定的源DataUnit srcFrame中的指定位置pos
     *  并指定存放个数
     *  对应的CmdType和CmdValueType为HEX
     * @return 转化并组装后的srcframe
     */
    public static DataUnit putHexStrToSrcFrame(DataUnit srcFrame, String cmd, int pos, int count){
        cmd = replaceBlank(cmd);
        for(int m = 0;m<count;m++){
            pos += cmd.length()*m;
            for (int i = 0; i < cmd.length(); i += 2) {
                srcFrame.bytes[pos + i / 2] = (byte) Integer.parseInt(cmd.substring(i, i + 2), 16);
            }
        }
        return srcFrame;
    }

    /**
     *  将指定十进制字符串cmd/cmdValue转换成十六机制byte数组，然后按指定字节大小 size 放入指定的源DataUnit srcFrame中的指定位置pos
     *  指定大小端 order: 2大端 1小端
     *  指定存放个数 count
     *  对应的CmdType和CmdValueType为DEC
     * @return 转化并组装后的srcframe
     */
    public static DataUnit putDecStrToSrcFrame(DataUnit srcFrame, String cmd, int pos, int count, int size, int order){
        cmd = replaceBlank(cmd);
        long lon = Long.parseLong(cmd);

        for(int m = 0;m<count;m++){
            // 根据大小端存储拷贝
            if (order == DataOrder.BIG_ENDIAN.getCode()) {
                // 大端拷贝
                for (int i = size - 1; i >= 0; i--) {
                    srcFrame.bytes[(size - 1-i) + pos+m*size] = (byte) (lon >> (i * 8) & 0xFF);
                }
            } else if(order == DataOrder.LITTLE_ENDIAN.getCode()){
                // 小端拷贝
                for (int i = 0; i < size; ++i) {
                    srcFrame.bytes[i + pos+m*size] = (byte) (lon >> (i * 8) & 0xFF);
                }
            }
        }
        return srcFrame;
    }



    private static void joinIdentities(DataUnit unit, List<Identity> identities) {
        if (identities == null || identities.size() == 0)
            return;
        for (Identity identity : identities) {
            String str = replaceBlank(identity.getValue());
            for (int i = 0; i < str.length(); i += 2) {
                unit.bytes[identity.getStartPos() + i / 2] = (byte) Integer.parseInt(str.substring(i, i + 2), 16);
            }
        }
    }
/*
    private static void joinFrameTypes(DataUnit unit, List<FrameType> frameTypes) {
        if (frameTypes == null || frameTypes.size() == 0)
            return;
        for (FrameType frameType : frameTypes) {
            String str = replaceBlank(frameType.getNewByteValue());
            //for (int i = 0; i < str.length(); i += 2) {
            //    unit.bytes[frameType.getBytePos() + i / 2] = (byte) Integer.parseInt(str.substring(i, i + 2), 16);
            //}
        }
    }
*/
    public static void joinChecks(DataUnit unit, List<CheckConfig> checks) {
        if (checks == null || checks.size() == 0)
            return;
        for (CheckConfig check : checks) {
            if (check.getCheckType().equalsIgnoreCase(CheckConfig.TYPE_SUM))
                joinSumCheck(unit, check);
            else
                System.err.println("暂时只支持和校验");
        }
    }

    /**
     * 加入校验
     */
    private static void joinSumCheck(DataUnit dataUnit, CheckConfig sumCheck) {
        // 计算和校验结果
        long sum = 0;
        for (int i = 0; i < sumCheck.getCheckLength(); i++) {
            sum += dataUnit.bytes[i + sumCheck.getCheckStartPos()] & 0xff;
        }

        // 根据大小端存储拷贝校验结果
        if (sumCheck.getResultByteOrder() == CheckConfig.BIG_INDIAN) {
            // 大端拷贝
            for (int i = sumCheck.getResultSize() - 1; i >= 0; i--) {
                dataUnit.bytes[i + sumCheck.getResultPos()] = (byte) (sum >> (i * 8) & 0xFF);
            }
        } else {
            // 小端拷贝
            for (int i = 0; i < sumCheck.getResultSize(); ++i) {
                dataUnit.bytes[i + sumCheck.getResultPos()] = (byte) (sum >> (i * 8) & 0xFF);
            }
        }
    }

    /**
     * 对数据进行校验
     */
    public static boolean checkData(byte[] bytes, List<CheckConfig> checks) {
        for (CheckConfig check : checks) {
            if (check.getCheckType().equalsIgnoreCase(CheckConfig.TYPE_SUM)) {
                long sum = 0;
                for (int i = 0; i < check.getCheckLength(); i++) {

                    sum += bytes[i + check.getCheckStartPos()];
                }

                long expectedSum = 0;
                // 根据大小端，取出在数据中的校验结果
                for (int i = 0; i < check.getResultSize(); i++) {
                    // 大端存储
                    if (check.getResultByteOrder() == CheckConfig.BIG_INDIAN) {
                        expectedSum = expectedSum * 10 + bytes[i + check.getResultPos()];
                    } else {
                        expectedSum = expectedSum * 10 + bytes[check.getResultPos() + check.getResultSize() - i - 1];
                    }
                }
                // 一处校验不通过，则直接返回校验失败
                if (sum != expectedSum) {
                    System.err.println("校验失败");
                    return false;
                }
            } else {
                System.err.println("暂时只支持和校验");
            }
        }
        return true;
    }
}
