package sy.databus.global;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Constants {

    // rightPane中成员字段的字体颜色
    public static final String COLOR_ITEM_NAME = "#494649FF";

    // 线条灰
    public static final String COLOR_DEFAULT_LINE = "#A6A6A6FF";

    // 选中项的蓝色背景
    public static final String COLOR_SEL_ITEM_BACKGROUND = "#578dd8";

    public static final String TITLED_HEAD_COMMON = "common";

    public static final String TITLED_HEAD_GENERIC = "generic";

    public static final String TABLE_TXT_DIR_PATH = ".\\tableTXT\\";

    public static final String VISUALIZATION_OUTPUT_PATH = ".\\vis\\";

    public static final String EXCEL_DIR_PATH = ".\\excel\\";

    public static final String CLASSIFYING_DIR_PATH = ".\\classifying\\";

    public static final String CONSOLE_TITLE = "综合处理器";

    public static final int MAX_PARAMS_ROWS = 32768;

    public static final Charset gCharset = Charset.forName("GBK");
}
