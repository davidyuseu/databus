package sy.common.util;



import sy.common.socket.entity.DataUnit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SComponentUtils {

    public static final int HEX = 16;
    public static final int DEC = 10;
    public static final int LITTLE_ENDIAN = 1;
    public static final int BIG_ENDIAN = 2;

    public static Map<String,Long> map_globalLastTime= new HashMap<>();

    public static enum DataOrder {
        LITTLE_ENDIAN(1),//小端
        BIG_ENDIAN(2);//大端


        private int code;

        private DataOrder(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public static enum DataNScale{
        DEC(10),
        HEX(16);

        private int code;
        private DataNScale(int code){
            this.code = code;
        }
        public int getCode() {
            return code;
        }

    }


    public static boolean isReachInterval(String timeKey, int interval){
        Long lastTime = map_globalLastTime.get(timeKey);
        Long crntTime = System.currentTimeMillis();

        if(lastTime==null)
        {//说明是第一次运行，故可返回true
            map_globalLastTime.put(timeKey,crntTime);
            return true;
        }else{
            if(crntTime-lastTime>=interval){//达到时间间隔
                map_globalLastTime.put(timeKey,crntTime);
                return true;
            }else{
                return false;
            }
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
        }else{
            return "";
        }
        return dest.trim();
    }

    /**
     * 删除字符串str中所有的空白字符并按指定字符s分割字符串
     * 一般用于combobox下拉选项value字符串为";"或","分割时
     * @return 处理后的字符串数组
     */
    public static String[] splitAndReplaceAllBlank(String str, String s){
        str = replaceBlank(str);
        return str.split(s);
    }

    /**
     *
     * 查找字符串srcStr中出现的指定字符串findStr的次数
     * @return 出现的字符串findStr次数
     */
    public static int countStrInStr(String srcStr,String findStr){
        int count = 0;
        Pattern pattern = Pattern.compile(findStr);
        Matcher matcher = pattern.matcher(srcStr);
        while (matcher.find()){
            count++;
        }
        return count;
    }

    /**
     * 按指定字符s分割字符串生成字符串数组
     * 删除字符串数组中每个字符串首尾的空白字符
     * 一般用于combobox下拉选项key字符串
     * @return 处理后的字符串数组
     */
    public static String[] splitAndTrim(String str, String s){
        if(str==null)
            str="";
        String[] strRes = str.split(s);
        int i = 0;
        while(i<strRes.length)
        {
            if(strRes[i]==null) {
                strRes[i] = "";
            }else {
                strRes[i] = strRes[i].trim();
            }
            i++;
        }
        return strRes;
    }

    /**
     *  将指定十六进制字符串cmd/cmdValue转换成十六机制byte数组，然后放入指定的源DataUnit srcFrame中的指定位置pos
     *  并指定存放个数
     *  对应的CmdType和CmdValueType为HEX (此时无需指定存放字节大小size和大小端order，因为其为手输字符串)
     * @return 转化并组装后的srcframe
     */
    public static DataUnit putHexStrToSrcFrame (DataUnit srcFrame, String cmd, int pos, int count){
        cmd = replaceBlank(cmd);
        for(int m = 0;m<count;m++){
            for (int i = 0; i < cmd.length(); i += 2) {
                srcFrame.bytes[pos + i / 2] = (byte) Integer.parseInt(cmd.substring(i, i + 2), 16);
            }
            pos += (cmd.length()/2);
        }
        return srcFrame;
    }

    /**
     *  将指定十六进制字符串cmd/cmdValue转换成十六机制byte数组，然后放入指定的源byte[] bytes中的指定位置pos
     *  并指定存放个数
     * @return 转化并组装后的bytes
     */
    public static byte[] putHexStrToSrcFrame (byte[] bytes, String cmd, int pos, int count){
        cmd = replaceBlank(cmd);
        for(int m = 0;m<count;m++){
            for (int i = 0; i < cmd.length(); i += 2) {
                bytes[pos + i / 2] = (byte) Integer.parseInt(cmd.substring(i, i + 2), 16);
            }
            pos += (cmd.length()/2);
        }
        return bytes;
    }

    /**
     *  将指定十进制字符串cmd/cmdValue转换成十六机制byte数组，然后按指定字节大小 size 放入指定的源DataUnit srcFrame中的指定位置pos
     *  指定大小端 order: 2大端 1小端
     *  指定存放个数 count
     *  对应的CmdType和CmdValueType为DEC
     * @return 转化并组装后的srcframe
     */
    public static DataUnit putDecStrToSrcFrame (DataUnit srcFrame, String cmd, int pos, int count, int size, int order){
        cmd = replaceBlank(cmd);
        long lon = Long.parseLong(cmd);

        for(int m = 0;m<count;m++){
            // 根据大小端存储拷贝
            if (order == DataOrder.BIG_ENDIAN.getCode()) {
                // 大端拷贝
                for (int i = size - 1; i >= 0; i--) {
                    srcFrame.bytes[(size - 1-i) + pos+m*size] = (byte) (lon >> (i * 8) & 0xFF);
                }
            } else if(order== DataOrder.LITTLE_ENDIAN.getCode()){
                // 小端拷贝
                for (int i = 0; i < size; ++i) {
                    srcFrame.bytes[i + pos+m*size] = (byte) (lon >> (i * 8) & 0xFF);
                }
            }
        }
        return srcFrame;
    }

    /**
     *  将指定的字符串数组转换成字符串
     * @return 转化后的字符串
     */
    public static String convertStrArrayToString(String[] strArr){
        if(strArr == null ||strArr.length == 0){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i<strArr.length;i++){
            if(strArr[i]==null){
                continue;
            }else if(strArr[i].trim().equals("")){
                continue;
            }
            sb.append(strArr[i]);
        }
        String res = sb.toString();
        return res;
    }

    /**
     * 将两个字符串trim后比较，为null的字符串按""处理
     * @return 比较结果
     */
    public static boolean trimEqual(String str1,String str2)
    {
        if(str1==null)
            str1="";
        if(str2==null)
            str2="";
        return str1.trim().equals(str2.trim());
    }

    /**
     * 将一个字符串str0截取到第一次出现字符串str1位置为止
     * @return 截取后的子字符串
     */
    public static String subStringOfStr(String str0,String str1)
    {
        if (str0==null)
            return "";
        if(str1==null)
            return "";
        int index = str0.indexOf("str1");
        if(index>=0)
        {
            return str0.substring(0,index);
        }else{
            return str0;//若str1在str0中不存在，则返回str0
        }
    }

    /**
     * 用来比较事件对象的Source或Target或PickResult的str0是否为str
     * toString()
     * trim()
     *
     * @return 比较结果
     */
    public static boolean eventTarOrSrcOrPResEqual(String str0, String str){
        if(str0==null|str0.equals(""))
            return false;
        if(str==null|str.equals(""))
            return false;
        int index =str0.indexOf("@");
        if(index<0)
            index = str0.indexOf("[");
        if(index<0)
        {
            System.err.println("event对象的Source|Target字符串中没有@|[");
            return false;
        }else {
            return str0.substring(0,index).trim().equals(str.trim());
        }


    }

    /**
     *  将指定的字符串srcStr按s分割成字符串数组，每个字符串去首位空白字符
     *  再将其中等于str（去首尾空白字符）的字符串去除后追加上r重组为新字符串
     * @return 转化后的字符串
     */
    public static String splitAndRemoveStr(String srcStr, String s, String str, String r){
        String[] strArr = splitAndTrim(srcStr,s);
        if(str==null){
            str ="";
        }
        String res = "";
        for(int i = 0; i<strArr.length;i++){
            if(strArr[i].equals(str.trim())){
                continue;//strArr[i]="";
            }else {
                res += strArr[i];
                if(i<strArr.length-1){
                    res +=r;
                }
            }
        }
        return res;
    }

    //获取ArrayList<Double>中的最小值
    public static double getMinFromdbArrayList (ArrayList<Double> dbArray){
        double min = dbArray.get(0);
        for(Double db:dbArray){
            if(db<min)
                min=db;
        }
        return min;
    }

    //获取ArrayList<Double>中的最大值
    public static double getMaxFromdbArrayList (ArrayList<Double> dbArray){
        double max = dbArray.get(0);
        for(Double db:dbArray){
            if(db>max)
                max=db;
        }
        return max;
    }

    /**
     *  对Map对象的深拷贝
     * @param paramsMap 被拷贝对象
     * @param resultMap 拷贝后的对象
     */
    public static void mapCopy(Map paramsMap, Map resultMap){
        if(resultMap == null) resultMap = new HashMap();
        if(paramsMap == null) return;

        Iterator it = paramsMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            Object key = entry.getKey();
            resultMap.put(key,paramsMap.get(key) != null ? paramsMap.get(key) : "");
        }
    }

    /**
     *由一个旧byte数组对象，深复制创建一个新的数组对象，并返回新数组
     */
    public static byte[] newBytesOf(byte[] oriBytes){
        if(oriBytes==null){
            System.err.println("源数组为空！");
            return null;
        }
        byte[] newBytes = new byte[oriBytes.length];
        System.arraycopy(oriBytes,0,newBytes,0,oriBytes.length);
        return newBytes;
    }

    /**
     *
     *  String左对齐
     */
    public static String padLeft(String src, int len, char ch) {
        int diff = len - src.length();
        if (diff <= 0) {
            return src;
        }

        for (int i = src.length(); i < len; i++) {
            src = ch + src;
        }
        return src;
    }
    /**
     *
     *  String右对齐
     */
    public static String padRight(String src, int len, char ch) {
        int diff = len - src.length();
        if (diff <= 0) {
            return src;
        }

        for (int i = src.length(); i < len; i++) {
            src = src + ch;
        }
        return src;
    }

    /**-DataBus------------------------------------------------------------------------------------------------*/
    /**
     * 由于HashMap的key，value均允许置入null，所以可使用该方法检查其空项。
     * */
    public static void nonNullMap(Map map) {
        Objects.requireNonNull(map, "The map cannot be null!");
        if(map instanceof ConcurrentHashMap) // ConcurrentHashMap本身就不允许存在key或value有null值。
            return;
        if(!(map instanceof TreeMap) && map.containsKey(null)) // TreeMap 本身就不允许存在key有null值。
            throw new NullPointerException("The map's keys contains null!");
        if(map.containsValue(null))
            throw new NullPointerException("The map's values contains null!");
    }
}
