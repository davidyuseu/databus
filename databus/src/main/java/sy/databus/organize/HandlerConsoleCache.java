package sy.databus.organize;

import javafx.scene.layout.Pane;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.common.fx.ui.MovableListView;
import sy.databus.process.Console.Category;
import sy.databus.process.IEventProc;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler的Console区的缓存资源
 *
 * ps：注意每新增一个缓存成员，都须在考虑在{@link #releaseCachedResource()}中释放之
 * */
public class HandlerConsoleCache {

    // 强缓存
    public static Map<Category, HandlersViewContext> handlersViewContextMap = new HashMap<>() {
        {
            put(Category.PRE_HANDLERS, null);
            put(Category.SERVICE_HANDLERS, null);
        }
    };

    public static MovableListView<IEventProc> getHandlersView(Pane content, Category category,
                                                                        SSyncObservableList<IEventProc> handlers) {
        if (!handlersViewContextMap.containsKey(category))
            throw new RuntimeException("unknown category!");

        HandlersViewContext handlersViewContext = handlersViewContextMap.get(category);
        if (handlersViewContext == null) {
            handlersViewContext = new HandlersViewContext(content, handlers);
            handlersViewContextMap.put(category, handlersViewContext);
        } else {
            handlersViewContext.configContext(content, handlers);
        }


        return handlersViewContext.getHandlersView();
    }

    // 仅在切换综合处理器时调用，释放上一次console区的缓存资源
    public static void releaseCachedResource() {
        // 释放handlersViewContextMap资源
        if (handlersViewContextMap != null && handlersViewContextMap.size() > 0) {
            for (Map.Entry<Category, HandlersViewContext> entry : handlersViewContextMap.entrySet()) {
                if(entry.getValue() != null) {
                    entry.getValue().release();
                }
            }
        }
    }
}
