package excel.annotation.format;

import java.lang.annotation.*;
import java.math.RoundingMode;

/**
 * Convert number format.
 *
 * <p>
 * write: It can be used on classes that inherit {@link Number}
 * <p>
 * read: It can be used on classes {@link String}
 *
 * @author Jiaju Zhuang
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface NumberFormat {

    /**
     *
     * Specific format reference {@link java.text.DecimalFormat}
     *
     * @return Format pattern
     */
    String value() default "";

    /**
     * Rounded by default
     *
     * @return RoundingMode
     */
    RoundingMode roundingMode() default RoundingMode.HALF_UP;
}
