package sy.common.util;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SStringUtil {
    /**
     * 删除字符串中空白字符
     *
     * @return 删除空白字符后的字符串
     */
    public static String removeBlank(String str){
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
     * @Description: 输入一个String[]，返回删除其中某元素的String[]
     * @param strs0 输入字符串数组
     * @param str   需要删除何种元素
     * @return String[]
     */
    public static String[] removeOf(String[] strs0, String str){
        if(strs0 == null || str == null)
            return null;
        List<String> strList = new ArrayList<>(Arrays.asList(strs0));
        for(int i = 0; i < strList.size(); i++){
            if(strList.get(i).equals(str)){
                strList.remove(i);
            }
        }
        return strList.toArray(new String[0]);
    }

    public static String[] removeEmptyString(@NonNull String[] strs0) {
        List<String> strList = new ArrayList<>(strs0.length);
        for (String str : strs0) {
            if (!str.replaceAll("[\\uE466 \\t\\r\\n]", "").isEmpty())
                strList.add(str);
        }
        String[] strs = new String[strList.size()];
        for (int i = 0; i < strs.length; i++) {
            strs[i] = strList.get(i);
        }
        return strs;
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
     * @Description: 将输入的Sting[]中包含tStrs子元素的元素中的子串都替换为rStr
     * @param strs0 被替换的字符串数组
     * @param rStr 替换成的字符串
     * @param gStrs 需要被替换的目标字符串集合
     * @return void
     */
    public static void replaceEachOf(String[] strs0, String rStr, String... gStrs){
        if(strs0 == null || rStr == null || gStrs == null)
            return;

        for(int i = 0; i < strs0.length; i++) {
            for(String str : gStrs) {
                strs0[i] = strs0[i].replace(str, rStr);
            }
        }
    }

    /**
     * 预处理表示十六进制的字符串，返回处理后的字符串，包含以下功能：
     * 1. 删除字符串中的空格、换行符、制表符
     * 2. 若字符串中含有非[^a-zA-Z0-9]的字符则抛出异常
     * 3. 若字符串长度不为偶数则抛出异常
     *
     * Input format:
     *  √ "eb900401"
     *  √ "eb 90 04 01"
     *  × "0x90eb" 不识别"0x"
     *  × "eb,90" 或 "eb，90" 或 "eb;90" 不识别非[a-zA-Z0-9]的字符
     *  × "eb90e" 长度不为偶数 且不为“0”
     * */
    public static String preEditHexInputString(String hexInputString) throws Exception{
        String dest = "";
        if(hexInputString!=null){
            // 删除空白字符
            Pattern p1 = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m1 = p1.matcher(hexInputString);
            dest = m1.replaceAll("");
            // 是否含有非十六进制字符
            Pattern p2 = Pattern.compile("[^a-fA-F0-9]");
            Matcher m2 = p2.matcher(dest);
            if(m2.find())
                throw new Exception("The input string contains non-hexadecimal characters!");
            if (dest.equals("0"))
                return "00";
            // 十六进制字符串长度是否为偶数
            if(dest.length() % 2 != 0)
                throw new Exception("The Hexadecimal string length is not even number!");
        }else{
            return "";
        }
        return dest;
    }


}
