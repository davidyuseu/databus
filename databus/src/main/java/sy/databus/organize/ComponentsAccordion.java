package sy.databus.organize;


import javafx.scene.control.Accordion;
import javafx.scene.layout.Pane;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.Processor;
import sy.databus.process.frame.handler.Handler;
import sy.databus.view.watch.EmptyProcessorPane;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Log4j2
public class ComponentsAccordion {

    private static Set<Class<?>> allProcClazzs = new HashSet<>();

    public static Set<Class<?>> reflectClassesInPackage(String pdgPath, Class annotation) {
        //1.扫描pdgPath包下的所有类
        Reflections reflections = new Reflections(pdgPath);
        //2.拿到包下类上有annotation注解的类
        return reflections.getTypesAnnotatedWith(annotation, true);  //获取有注解的类
    }

    public static Set<Class<?>> reflectClassesInPackage(Class childClazz, Class annotation) {
        return reflectClassesInPackage(childClazz.getPackageName(), annotation);
    }

    public static void arrangeProcessorClasses(Accordion outerContainers, Class opClazz) {
        Set<Class<?>> processorClasses = reflectClassesInPackage(opClazz, Processor.class);
        for (Class clazz : processorClasses) {
            Processor processorAnno = (Processor) clazz.getAnnotation(Processor.class);
            Pane outerContainer = ContainerTool.getScrolledOuterContainer(outerContainers,
                    processorAnno.type().getCategory().getName());
            outerContainer.getChildren().add(new ComponentsItem<>(clazz));
        }
    }

    public static void arrangeProcessorClassesWithWatchPane(Accordion outerContainers,
                                                            Class<?> opClazz) {
        var processorClasses = reflectClassesInPackage(opClazz, Processor.class);
        for (Class<?> clazz : processorClasses) {
            Processor processorAnno = clazz.getAnnotation(Processor.class);
            if (processorAnno == null || allProcClazzs.contains(clazz)) {
                continue;
            } else {
                allProcClazzs.add(clazz);
            }
            Pane outerContainer = ContainerTool.getScrolledOuterContainer(outerContainers,
                    processorAnno.type().getCategory().getName());
            if (!EmptyProcessorPane.class.isAssignableFrom(processorAnno.pane())) { // WatchPane非空则列出显示
                ComponentsItem componentsItem = new ComponentsItem(clazz);
                if (processorAnno.coupledParents()[0] != Processor.DEFAULT_COUPLE
                        && processorAnno.coupledParents().length == 1) {
                    Processor parentAnno = processorAnno.coupledParents()[0].getAnnotation(Processor.class);
//                    if (parentAnno.coupledSubs().length == 1 && parentAnno.coupledSubs()[0] == clazz) // 一对一
                    if (parentAnno.coupledSubs().length > 0 && List.of(parentAnno.coupledSubs()).contains(clazz)) // 一对多
                        BaseComponentsItem.addComponentWithBinding(componentsItem, processorAnno.coupledParents()[0]);
                } else {
                    Pane internalContainer = ContainerTool.getInternalContainer(outerContainer, processorAnno.group());
                    internalContainer.getChildren().add(componentsItem);
                }
            }
        }
    }

    public static void arrangeHandlerClasses(Accordion outerContainers, Class opClazz) {
        Set<Class<?>> handlerClasses = reflectClassesInPackage(opClazz, Handler.class);
        for (Class clazz : handlerClasses) {
            Handler handlerAnno = (Handler) clazz.getAnnotation(Handler.class);
            Pane outerContainer = ContainerTool.getScrolledOuterContainer(outerContainers,
                    handlerAnno.category().getTitle());
            Pane internalContainer = ContainerTool.getInternalContainer(outerContainer, handlerAnno.group());
            internalContainer.getChildren().add(new ComponentsItem(clazz));
        }
    }

    /** 将待绑定的{@link BaseComponentsItem} 添加到其父组件中*/
    public static void arrangeComponentWithBinding() {
        ComponentsItem.getComponentsWithBinding().stream().forEach(componentsItem -> {
            BaseComponentsItem.getComponent(componentsItem.getBindingClazz())
                    .addToBindingPane(componentsItem);
        });
    }
}
