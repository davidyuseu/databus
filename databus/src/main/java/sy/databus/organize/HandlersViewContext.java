package sy.databus.organize;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;
import sy.common.cache.CacheFactory;
import sy.common.cache.ICache;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.common.fx.ui.MovableListView;
import sy.databus.MainPaneController;
import sy.databus.UISchedules;
import sy.databus.global.GlobalState;
import sy.databus.global.WorkMode;
import sy.databus.process.IEventProc;
import sy.databus.process.internal.OriDataViewHandler;

import java.util.Set;

import static sy.databus.view.controller.ConsoleController.STYLE_CLASS_CACHED_CONTROLLER;

/** Pre Handlers 或 Service Handlers的上下文*/
public class HandlersViewContext {

    @Getter @Setter
    private MovableListView<IEventProc> handlersView;

    private Pane focusedHandlerContainer; // 上一次的焦点handler组件容器

    private IEventProc focusedHandler;

    private ChangeListener<WorkMode> workModeListener = (ob, oldValue, newValue) -> {
        checkEditability(newValue);
        handlersView.setEditable(GlobalState.currentWorkMode.get() == WorkMode.EDIT);
    };

    private void checkEditability(WorkMode newValue) {
        if (newValue == WorkMode.ANALYSIS) {
            // 使Console中的handler组件不可编辑
            handlersView.forbidEditing();
        } else if (newValue == WorkMode.EDIT) {
            // 使Console中的handler组件可编辑
            handlersView.allowEditing();
        }
    }

    public HandlersViewContext(Pane container, SSyncObservableList<IEventProc> handlers) {
        this.focusedHandlerContainer = container;

        handlersView = new MovableListView<IEventProc>(handlers,
                GlobalState.currentWorkMode.get() == WorkMode.EDIT) {
            @Override
            protected String getItemString(IEventProc item) {
                if (item instanceof OriDataViewHandler) {
                    return "  ↑ 原码显示（输出）";
                } else {
                    return item.getClass().getSimpleName();
                }
            }
        };

        handlersView.getListView().setMinHeight(178d);

        // 设置handlersView中焦点的监听，从而使焦点改变时反射controller到需要呈现的container中
        handlersView.getListView().getSelectionModel().selectedIndexProperty()
                .addListener((observableValue, oldNum, newNum) -> {
                    if (focusedHandlerContainer != null) {
                        /** 删除当前焦点handler的各controller*/
                        if (oldNum.intValue() >= 0) {
                            uncustomiseAndRecycleHandler();
                        }
                        if (newNum.intValue() >= 0) { // t1为-1时说明是“×”操作
                            cunstomiseAndAdaptHandler(newNum);
                        }
                    }
                });
        handlersView.getListView().addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    if (focusedHandler == null
                            || focusedHandler instanceof OriDataViewHandler)
                        return;
                    // handler的原码显示只能在任务模式下进行
                    if (GlobalState.currentWorkMode.get() != WorkMode.EDIT) {
                        MainPaneController.reloadOriDataView(focusedHandler);
                    }
                }
            }
        });
        /** 此处必须重新提交到FX线程池中，否则handler组件还未被界面显示出来，changed方法中找不到各".handlerBtn"控件 */
        GlobalState.currentWorkMode.addListener(workModeListener);

    }

    private void cunstomiseAndAdaptHandler(Number t1) {
        focusedHandler = handlersView.getListView().getItems().get(t1.intValue());
        if (focusedHandler instanceof OriDataViewHandler) // 不处理原码显示handler
            return;
        ConsoleManager.adaptHandler(focusedHandlerContainer, focusedHandler);
    }

    private void uncustomiseAndRecycleHandler() {
        synchronized (UISchedules.consoleUILock) { // 可重入锁，当消除Processor时会重入该锁
            // 对上一个handler的属性面板去自定义化
            // ps:即使组件被setVisible(false)或setManaged(false)，也可以通过以下方式找到，只要它已经被show在界面上
            Set<Node> controllers = focusedHandlerContainer.lookupAll("." + STYLE_CLASS_CACHED_CONTROLLER);
            if (focusedHandler != null && focusedHandler instanceof ComponentCustomization customization)
                customization.uncustomize(controllers);
            focusedHandlerContainer.getChildren().removeAll(focusedHandlerContainer.getChildren());
            for (Node node : controllers) {
                CacheFactory.recycle((ICache) node);
            }
            focusedHandler = null;
        }
    }

    public void configContext(Pane container, SSyncObservableList<IEventProc> handlers) {
        this.focusedHandlerContainer = container;
        handlersView.setSyncObList(handlers);
        workModeListener.changed(null, null, GlobalState.currentWorkMode.get());
    }

    public void release() {
        if (focusedHandlerContainer != null) {
            // 删除当前handler的各controller
            Set<Node> controllers = focusedHandlerContainer.lookupAll("." + STYLE_CLASS_CACHED_CONTROLLER);
            focusedHandlerContainer.getChildren().removeAll(focusedHandlerContainer.getChildren());
            for (Node node : controllers) {
                CacheFactory.recycle((ICache) node);
            }
            /** 选到-1即失去焦点，从而触发{@link #uncustomiseAndRecycleHandler}*/
            handlersView.getListView().getSelectionModel().select(-1); //
        }
    }
}
