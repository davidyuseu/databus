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
public @interface HeadRowHeight {
    /**
     * Set the header height
     * <p>
     * -1 mean the auto set height
     *
     * @return Header height
     */
    short value() default -1;
}
