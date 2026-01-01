package sy.databus.organize;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.layout.Pane;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.common.fx.ui.MovableListView;
import sy.databus.UISchedules;
import sy.databus.entity.property.*;
import sy.databus.global.GlobalState;
import sy.databus.global.WorkMode;
import sy.databus.process.*;
import sy.databus.process.Console.Config;
import sy.databus.process.Console.Report;
import sy.databus.view.container.ScrolledContent;
import sy.databus.view.controller.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

import static sy.databus.process.Console.Category.*;

/**
 * 综合处理器的监控台管理器
 * */
@Log4j2
public class ConsoleManager {

    // Console区的缓存资源
//    public static Map<String, Object> consoleCachedResource = new HashMap<>();
    // 对Field解析获取Controller的策略接口
    private interface ControllerFactory {
        default ConsoleController checkFieldAndGetController(Field field, Console console, Object parentObj) throws IllegalAccessException {
            field.setAccessible(true);
            if (field.get(parentObj) == null) {
                log.warn(parentObj.getClass().getSimpleName() + " 中的" + field.getName() + "字段未初始化(null)!");
            }
            return getController(field, console, parentObj);
            /**
             * 使用controller来配置handler或processor，被配置的字段若为（未初始化的）null，则其相当于final字段
             * 考虑后续在controller中重新初始化该字段对象时，因为field.set(parentObj, value)未加锁，一定要注意可能会引发并发问题
             * */
//            else throw new RuntimeException("controller cannot accept null field obj!");
        }
        ConsoleController getController(Field field, Console console, Object obj) throws IllegalAccessException;
    }

    // 获取IProperty类型的字段的接口
    private static class PropertyControllerFactory implements ControllerFactory {

        @Override
        public ConsoleController getController(Field field, Console console, Object obj) throws IllegalAccessException {
            if (!IProperty.class.isAssignableFrom(field.getType()))
                return null;
            IProperty property = (IProperty) field.get(obj);
            if(ConsecutiveBufSafeProperty.class.isAssignableFrom(field.getType()))
                return ConsecutiveBufController.buildController(console, field, obj, (ConsecutiveBufSafeProperty) property);
            return null;
        }
    }

    private static class BasicInputControllerFactory implements ControllerFactory {

        @Override
        public ConsoleController getController(Field field, Console console, Object obj) throws IllegalAccessException {
            Type fieldType = field.getGenericType();
            return BasicInputController.buildController(fieldType, console, field, obj);
        }
    }

    private static class CustomControllerFactory implements ControllerFactory {

        @Override
        public ConsoleController getController(Field field, Console console, Object obj) throws IllegalAccessException {
            Class fieldClazz = field.getType();
            if (SFile.class.isAssignableFrom(fieldClazz)) {
                return SFileController.buildController(console, field, obj, (SFile) field.get(obj));
            }
            if (MultiSelNumParamList.class.isAssignableFrom(fieldClazz)) {
                return MultiSelNumParamListController.buildController(console, field, obj, (MultiSelNumParamList) field.get(obj));
            }
            else if (MultiSelCharParamList.class.isAssignableFrom(fieldClazz)) {
                return MultiSelCharParamListController.buildController(console, field, obj, (MultiSelCharParamList) field.get(obj));
            }
            else if (MultiSelGenParamList.class.isAssignableFrom(fieldClazz)) {
                return MultiSelGenParamListController.buildController(console, field, obj, (MultiSelGenParamList) field.get(obj));
            }
            else if (AbstractMultiSelectObList.class.isAssignableFrom(fieldClazz)) {
                return MultiSelObListController.buildController(console, field, obj, (AbstractMultiSelectObList) field.get(obj));
            }
            if (RadioSelectObList.class.isAssignableFrom(fieldClazz)) {
                return RadioSelObListController.buildController(console, field, obj, (RadioSelectObList) field.get(obj));
            }
            return null;
        }
    }

    private static class BasicCheckBoxControllerFactory implements ControllerFactory {

        @Override
        public ConsoleController getController(Field field, Console console, Object obj) throws IllegalAccessException {
            Type fieldType = field.getGenericType();
            return BasicCheckBoxController.buildController(fieldType, console, field, obj);
        }
    }

    private static final List<ControllerFactory> controllerFactories = new ArrayList<>(){
        {
            add(new BasicInputControllerFactory());
            add(new BasicCheckBoxControllerFactory());
            add(new CustomControllerFactory());
            add(new PropertyControllerFactory());
        }
    };

    @SneakyThrows
    /** 对于processor而言，返回的ConsoleController集合都是PROPERTIES属性面板中的组件*/
    public static void adaptProcessorController(Accordion outerContainers, AbstractIntegratedProcessor processorObj) {
        synchronized (UISchedules.consoleUILock) {
            if (processorObj == null) {
                log.error("'processorObj' is null, suggest that check if there is any unreleased reference to the parent nodeSkin which has been deleted!");
                return;
            }
            Class<?> clazz = processorObj.getClass();
            List<String> fieldsIgnore;
            if (!clazz.isAnnotationPresent(Processor.class)) {
                log.warn("This processor has no annotation of 'Processor'!");
                return;
            } else {
                fieldsIgnore = Arrays.asList(
                        clazz.getAnnotation(Processor.class).fieldsIgnore());
            }
            Pane propertiesContainer = null;
            // 对于processor而言，返回的ConsoleController集合都是PROPERTIES属性面板中的组件
            Set<Node> controllers = new HashSet<>();
            while (clazz != null && clazz != Object.class) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Console.class)) {
                        if (fieldsIgnore.contains(field.getName()))
                            continue;
                        Console console = field.getAnnotation(Console.class);
                        Console.Category category = console.category();
                        if (category == PROPERTIES) {
                            if ((console.config() == Config.NONE || console.config() == Config.IMPLICIT)
                                    && console.report() == Report.NONE) // 此种情况不需要Controller
                                continue;
                            if (propertiesContainer == null) {
                                propertiesContainer =
                                        ContainerTool.getScrolledOuterContainer(outerContainers, PROPERTIES.getName());
                            }
                            // 当字段的category为normal时，将字段的controller添加到rightPane的“属性”(Category.PROPERTIES)一栏中
                            controllers.add(adaptFieldController(propertiesContainer, field, console, processorObj));
                        } else if (category == CORE_HANDLER) {
                            // >> 对于corehandler
                            IEventProc handler;
                            try {
                                field.setAccessible(true);
                                handler = (IEventProc) field.get(processorObj);
                            } catch (Exception e) {
                                throw new ProcessorInitException("error occur when getting coreHandler by reflecting the field!", e);
                            }
                            Pane outerContainer =
                                    ContainerTool.getScrolledOuterContainer(outerContainers, category.getName());
                            if (handler != null)
                                adaptHandler(outerContainer, handler);
                            else
                                log.warn("{}", "the coreHandler is null, cannot adapt controller for it!");
                        } else if (category == PRE_HANDLERS || category == SERVICE_HANDLERS) {
                            SSyncObservableList<IEventProc> handlers;
                            try {
                                field.setAccessible(true);
                                handlers = (SSyncObservableList<IEventProc>) field.get(processorObj);
                            } catch (Exception e) {
                                throw new ProcessorInitException("error occur when getting handlers by reflecting the field!", e);
                            }
                            /** 由于生产者/传输者（消费者）/转换者中handlers的使用情况不尽相同，所以这里注意仅适配非空的handlers*/
                            if (handlers != null) {
                                Pane outerContainer =
                                        ContainerTool.getNonScrolledOuterContainer(outerContainers, category.getName());
                                adaptHandlers(outerContainer, category, handlers);
                            }
                        } else {
                            log.error("{}", "unable to adapt the category!");
                        }
                    }
                }
                if (clazz != AbstractIntegratedProcessor.class)
                    clazz = clazz.getSuperclass();
                else
                    break;
            }
            // 对当前processor的"Properties"属性面板自定义
            if (controllers.size() > 0)
                processorObj.customise(controllers);
        }
    }

    // non scrolled outerContainer
    private static void adaptHandlers(Pane outerContainer, Console.Category category, SSyncObservableList<IEventProc> handlers) {
        ScrolledContent scrolledContent = ContainerTool.createScrolledContent(outerContainer);
        MovableListView<IEventProc> handlersView
                = HandlerConsoleCache.getHandlersView((Pane) scrolledContent.getContent(), category, handlers); // scrolledContent.getContent() -> focusedPane
        outerContainer.getChildren().add(handlersView.getListView());
        // 承载handlers的listView不应该置于scrollPane中，否则scrollPane中的scrollbar会遮挡listView的scrollbar
        outerContainer.getChildren().add(scrolledContent);
    }

    @SneakyThrows
    /** 对于handler而言，返回的ConsoleController集合都是handlers列表下方unScrolledPane属性面板中的组件*/
    public static void adaptHandler(Pane outContainer, IEventProc handler) {
//        ConsoleCache.currentHandlerRef = handler; // 缓存当前正在编辑的handler的引用
        synchronized (UISchedules.consoleUILock) {
            Class<?> clazz = handler.getClass();
            Set<Node> controllers = new HashSet<>();
            while (clazz != null && clazz != Object.class) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Console.class)) {
                        Console console = field.getAnnotation(Console.class);
                        if ((console.config() == Config.NONE || console.config() == Config.IMPLICIT)
                                && console.report() == Report.NONE) // 此种情况不需要Controller
                            continue;
                        controllers.add(adaptFieldController(outContainer, field, console, handler));
                    }
                }
                if (clazz != AbstractHandler.class) // #- if (clazz.getSuperclass() != IEventProc.class)
                    clazz = clazz.getSuperclass();
                else
                    break;
            }

            if (handler instanceof ComponentCustomization customization) {
                customization.customise(controllers);
            }
        }
    }

    /** 反射构建Controller的主要方法*/
    private static ConsoleController adaptFieldController(Pane root, Field field, Console console, Object obj) throws IllegalAccessException {
        Pane container = ContainerTool.getInternalContainer(root, console.group());
        ConsoleController controllerNode = null;
        for (ControllerFactory factory : controllerFactories) {
            controllerNode = factory.checkFieldAndGetController(field, console, obj);
            if (controllerNode != null) {
                break;
            }
        }
        if (controllerNode == null) {
            throw new ProcessorInitException("Cannot adapt the unknown field type!");
        } else {
            controllerNode.setId(field.getName());
            ChangeListener<WorkMode> listener = controllerNode.getWorkModeChangeListener();
            /** 使用弱引用，防止被弃用或换用的controller依然保持对{@link GlobalState.currentWorkMode}的引用 */
            if (listener != null) {
                GlobalState.currentWorkMode.addListener(new WeakChangeListener<>(listener));
                /** 1. 对于拥有监听器的controller，触发一次当前对workMode的监听，以同步当前针对workMode的状态 */
                listener.changed(GlobalState.currentWorkMode, null, GlobalState.currentWorkMode.get());
            } else {
                /** 2. 对于监听器为null的controller，直接设置其编辑状态，
                 * 因为它的editable已经在{@link ConsoleController.WorkFeature#setWorkModeListener(ConsoleController, Console)}
                 * 中完成设置
                 * */
                controllerNode.setUneditable(!controllerNode.isEditable());
            }
            container.getChildren().add(0, controllerNode);
        }
        return controllerNode;
    }
}
