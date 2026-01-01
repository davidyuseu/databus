package sy.databus.global;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import lombok.extern.log4j.Log4j2;
import sy.common.cache.CacheFactory;
import sy.common.cache.ICache;
import sy.databus.RightPaneController;
import sy.databus.UISchedules;
import sy.databus.organize.HandlerConsoleCache;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.grapheditor.api.GNodeSkin;

import java.util.Set;

import static sy.databus.process.Console.Category.PROPERTIES;
import static sy.databus.view.container.Constants.STYLE_CLASS_CACHED_PANE;
import static sy.databus.view.controller.ConsoleController.STYLE_CLASS_CACHED_CONTROLLER;

@Log4j2
public class GlobalState {

    public static InitConfig initConfig;

    /**
     * 2字节，标识当前席位号
     * */
    public static short seatNum = 1;

    // 当前选中的watch节点
    public static SimpleObjectProperty<GNodeSkin> currentNodeSkin = new SimpleObjectProperty<>(null);
    public static synchronized void setCurrentNodeSkin(GNodeSkin nodeSkin) {
        currentNodeSkin.set(nodeSkin);
    }
    // 当前的工作模式
    public static SimpleObjectProperty<WorkMode> currentWorkMode = new SimpleObjectProperty<>(WorkMode.ORIGINAL) {

        @Override
        protected void invalidated() {
            super.invalidated();

            if (get().equals(WorkMode.EDIT)) {
                // 打开左编辑组件栏
                // 打开编辑开关
            } else {
                // 隐藏左编辑组件栏
                // 关闭编辑开关
                if (get().equals(WorkMode.ANALYSIS)) {

                } else if (get().equals(WorkMode.MISSION)) {

                } else {
                    log.error("undefined work mode!");
                }
            }
        }
    };

    public static void recycleRightPaneResources() {
        synchronized (UISchedules.consoleUILock) {
            var displayedProcessor = RightPaneController.INSTANCE.getDisplayedProcessor();
            if (displayedProcessor == null) {
                return;
            }
            Accordion outContainers = RightPaneController.INSTANCE.getOuterContainers();
            if (outContainers.getPanes().size() == 0)
                return;
            // 释放或回收当前processor中properties中的所有缓存资源
            // TODO 注意：目前对于不在Properties栏中的processor组件不会被回收
            uncustomizeAndRecycleProcessor(outContainers, displayedProcessor);
            // 释放或回收当前processor中pre-handlers和service-handlers的所有缓存资源，防止内存泄漏
            HandlerConsoleCache.releaseCachedResource();
            recycleCachedPanes(outContainers);
            RightPaneController.INSTANCE.setDisplayedProcessor(null);
        }
    }

    private static void recycleCachedPanes(Accordion outContainers) {
        Set<Node> panes = outContainers.lookupAll("." + STYLE_CLASS_CACHED_PANE);
        outContainers.getPanes().removeAll(outContainers.getPanes());
        // 删除并回收当前processor所有的"cachedPane"（左右边栏中可复用的容器面板）
        for (Node node : panes) {
            CacheFactory.recycle((ICache) node);
        }
    }

    static {
        currentNodeSkin.addListener((observableValue, gNodeSkin, t1) -> {
            if (t1 == null) {
                recycleRightPaneResources();
                RightPaneController.setTitle(Constants.CONSOLE_TITLE);
            }
        });
    }

    private static void uncustomizeAndRecycleProcessor(Accordion outContainers, AbstractIntegratedProcessor displayedProcessor) {
        Node propertiesPanel = outContainers.lookup("#" + PROPERTIES.getName());
        if (propertiesPanel != null) {
            Set<Node> controllers = propertiesPanel.lookupAll("." + STYLE_CLASS_CACHED_CONTROLLER);
            displayedProcessor.uncustomize(controllers);
            // 删除并回收当前processor所有的"cachedController"（可复用组件）
            for (Node node : controllers) {
                CacheFactory.recycle((ICache) node);
            }
        }
    }

    public static void Init() {
        ProcessorSJsonUtil.init();
        initConfig = InitConfig.getInstance();
    }
}
