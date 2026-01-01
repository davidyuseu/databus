package excel.annotation;

import java.lang.annotation.*;

/**
 * Ignore convert excel
 *
 * @author Jiaju Zhuang
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelIgnore {}
