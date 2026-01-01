package excel.annotation.write.style;

import java.lang.annotation.*;

/**
 * Merge the cells once
 *
 * @author Jiaju Zhuang
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface OnceAbsoluteMerge {
    /**
     * First row
     *
     * @return
     */
    int firstRowIndex() default -1;

    /**
     * Last row
     *
     * @return
     */
    int lastRowIndex() default -1;

    /**
     * First column
     *
     * @return
     */
    int firstColumnIndex() default -1;

    /**
     * Last row
     *
     * @return
     */
    int lastColumnIndex() default -1;
}
