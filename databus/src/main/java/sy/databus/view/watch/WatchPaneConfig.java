package sy.databus.view.watch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WatchPaneConfig {
    double initialHeight() default 0.0;
    double initialWidth() default 0.0;
}
