package excel.annotation.write.style;

import java.lang.annotation.*;

/**
 * Set the height of each table
 *
 * @author Jiaju Zhuang
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ContentRowHeight {

    /**
     * Set the content height
     * <p>
     * -1 mean the auto set height
     *
     * @return Content height
     */
    short value() default -1;
}
