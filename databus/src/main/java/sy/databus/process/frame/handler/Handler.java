package sy.databus.process.frame.handler;

import sy.databus.process.IEventProc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static sy.databus.global.Constants.TITLED_HEAD_COMMON;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handler {
    String DEFAULT_NAME = "Handler";
    enum Category {
        HANDLER_FRAME("Frame Handler"),
        HANDLER_PREHANDLER("Frame PreHandler"), // 该Handler只能被嵌入到PROCESSOR_DISK_IO类型的Processor中的preHandlers中
        HANDLER_SERVICE("Frame Service"), // 该Handler只能被嵌入到PROCESSOR_DISK_IO类型的Processor中的serviceHandlers中
        HANDLER_IO("IO Service"); // 该Handler只能被嵌入到PROCESSOR_DISK_IO类型的Processor中serviceHandlers中

        String title;

        Category(String title) {
            this.title = title;
        }

        public String getTitle(){
            return title;
        }
    }

    Category category();

    String group() default TITLED_HEAD_COMMON;

    String name() default DEFAULT_NAME;

}
