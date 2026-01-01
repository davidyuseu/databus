package usermodel;

import org.apache.poi.ss.usermodel.Row;


/**
 * utils
 *
 * @author Jiaju Zhuang
 */
public class PoiUtils {

    /**
     * Whether to customize the height
     */
//    public static final BitField CUSTOM_HEIGHT = BitFieldFactory.getInstance(0x640);

    /**
     * Whether to customize the height
     *
     * @param row row
     * @return
     */
    public static boolean customHeight(Row row) {

        return false;
    }
}
