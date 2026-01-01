package sy.databus.process;

import sy.common.util.SStringUtil;
import sy.databus.view.controller.ConsoleController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static sy.databus.global.Constants.TITLED_HEAD_COMMON;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Console {
    /**
     * {@link sy.databus.view.controller.ConsoleController.WorkFeature#setWorkModeListener(ConsoleController, Console)}
     * */
    enum Config {
        /** （默认）不参与配置化（既不序列化，且在Console配置界面中不可配置，列如：该字段仅参与回报）*/
        NONE,
        /** 隐式地进行序列化和反序列化，在Console配置界面中不可配置（如PId，1.不可配置；2.须序列化；）*/
        IMPLICIT,
        /** 静态配置，编辑阶段转任务阶段时与Console配置模块交互一次*/
        STATIC,
        /** 动态配置，编辑阶段和任务阶段中均可响应Console的配置，请在充分考虑并发问题后谨慎使用*/
        DYNAMIC,
        /** 非运行状态时可配置，如编辑模式和分析模式下的任务未开启阶段*/
        NON_RUNNING
    }
    enum Report {
        NONE,
        RUNTIME
    }
    enum Category {
        PROPERTIES("属性"),
        CORE_HANDLER("Core Handler"),
        PRE_HANDLERS("预处理层"),
        SERVICE_HANDLERS("业务层");

        String name;

        Category(String name) {
            this.name = name;
        }

        public String getName(){
            return name;
        }

        public static Category valueOfNonBlankName(String name) {
            for(Category category : Category.values()) {
                if (SStringUtil.removeBlank(category.getName()).equals(name))
                    return category;
            }
            return null;
        }
    }

    Config config() default Config.NONE;
    Report report() default Report.NONE;
    String display() default "";
    String reg() default "";
    Category category() default Category.PROPERTIES;
    String group() default TITLED_HEAD_COMMON;
}
