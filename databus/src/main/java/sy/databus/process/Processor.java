package sy.databus.process;

import sy.databus.global.ProcessorType;
import sy.databus.view.watch.IProcessorPane;

import java.lang.annotation.*;

import static sy.databus.global.Constants.TITLED_HEAD_COMMON;
import static sy.databus.global.Constants.TITLED_HEAD_GENERIC;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Processor {
    String DEFAULT_FIELDS_IGNORE = "";

    interface DefaultBindingMark extends IEventProc{}
    Class<DefaultBindingMark> DEFAULT_COUPLE = DefaultBindingMark.class;

    String group() default TITLED_HEAD_COMMON;

    ProcessorType type();

    Class<? extends IProcessorPane> pane();

    String[] fieldsIgnore() default DEFAULT_FIELDS_IGNORE;

    /** bind to the processor which is the admitted parent*/
    Class<? extends IEventProc>[] coupledParents() default DefaultBindingMark.class;

    /** bind to the processor which is the admitted subclass*/
    Class<? extends IEventProc>[] coupledSubs() default DefaultBindingMark.class;
}
