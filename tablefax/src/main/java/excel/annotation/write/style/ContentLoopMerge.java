package excel.annotation.write.style;

import java.lang.annotation.*;

/**
 * The regions of the loop merge
 *
 * @author Jiaju Zhuang
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ContentLoopMerge {
    /**
     * Each row
     *
     * @return
     */
    int eachRow() default 1;

    /**
     * Extend column
     *
     * @return
     */
    int columnExtend() default 1;
}
