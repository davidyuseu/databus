package excel.annotation;

import java.lang.annotation.*;

/**
 * Ignore all unannotated fields.
 *
 * @author Jiaju Zhuang
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelIgnoreUnannotated {
}
