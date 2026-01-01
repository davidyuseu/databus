package sy.databus.view.controller;

import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;
import sy.common.cache.ICache;
import sy.databus.global.WorkMode;
import sy.databus.process.Console;

import java.lang.reflect.Field;

public abstract class ConsoleController extends Pane implements ICache {

    public static final String STYLE_CLASS_CACHED_CONTROLLER = "cachedController";

    @Getter @Setter
    protected boolean editable = true;

    @Getter @Setter
    protected ChangeListener<WorkMode> workModeChangeListener;

    // 字段
    protected Field field;
    // 该字段对应的对象
    @Getter
    protected Object parentObj;
    @Getter
    protected Console annoConsole;

    public enum WorkFeature {
        EMPTY { /** */
            @Override
            public void setWorkModeListener(ConsoleController controller, Console console) {
                // empty impl
            }
        },
        STATIC {
            @Override
            public void setWorkModeListener(ConsoleController controller, Console console) {
                if(console.config() == Console.Config.NONE
                        || console.config() == Console.Config.IMPLICIT) {
                     controller.editable = false;
                } else { // Config.STATIC || Config.DYNAMIC
                    /** 当config()是静态的，此时无所谓report()属性是否为实时回报*/
                    controller.workModeChangeListener =
                            (observableValue, oldMode, newMode) -> controller.setUneditable(newMode != WorkMode.EDIT);
                }
            }
        },
        DYNAMIC {
            @Override
            public void setWorkModeListener(ConsoleController controller, Console console) {
                if(console.config() == Console.Config.NONE
                        || console.config() == Console.Config.IMPLICIT) {
                    if (!customizeControl(controller)) {
                        controller.editable = false;
                    }
                } else if (console.report() != Console.Report.NONE) { // (Config.STATIC || Config.DYNAMIC) && Report.RUNTIME
                    /** 如果回报权限是非NONE的，则默认config()是非动态的（防止“配置”动作与“回报”动作冲突）*/
                    if (!customizeControl(controller)) {
                        controller.workModeChangeListener =
                                (observableValue, oldMode, newMode) -> controller.setUneditable(newMode != WorkMode.EDIT);
                    }
                } else if (console.config() == Console.Config.STATIC) { // Config.STATIC && Report.NONE
                    if (!customizeControl(controller)) {
                        controller.workModeChangeListener =
                                (observableValue, oldMode, newMode) -> controller.setUneditable(newMode != WorkMode.EDIT);
                    }
                } else { // Config.DYNAMIC && Report.NONE
                    if (!customizeControl(controller)) {
                        controller.editable = true;
                    }
                }
            }

            private boolean customizeControl(ConsoleController controller) {
                if (controller instanceof MSObListControl msObListControl) {
                    controller.workModeChangeListener = (observableValue, oldMode, newMode) -> {
                        controller.editable = true;
                        msObListControl.getTfFrameLen().setDisable(newMode != WorkMode.EDIT);
                    };
                    return true;
                }
                return false;
            }
        };

        public abstract void setWorkModeListener(ConsoleController controller, Console console);
    }

    enum ControllerState{
        NORMAL,
        INPUT_ABNORMAL,
        REPORT_ABNORMAL
    }
    protected ControllerState state = ControllerState.NORMAL;

    public ConsoleController (Console console, Field field, Object parentObj) {
        this.getStyleClass().add(STYLE_CLASS_CACHED_CONTROLLER);
        this.setPrefWidth(238d);

        /*-- build----------------------------------------------------------------------------------------------*/
        build(console, field, parentObj);
    }

    private void build(Console console, Field field, Object parentObj) {
        this.field = field;
        this.field.setAccessible(true);
        this.parentObj = parentObj;
        this.annoConsole = console;
        WorkFeature.DYNAMIC.setWorkModeListener(this, console);
    }

    public void rebuild(Console console, Field field, Object parentObj) {
        editable = true;
        build(console, field, parentObj);
    }

    /**
     * 设置controller是否可编辑，可被子controller重写，如{@link AbstractCheckBoxController}、{@link AbstractTextFieldController}
     * 默认实现是将整个Pane设置为disable
     * */
    public void setUneditable(boolean uneditable) {
        editable = !uneditable;
        this.setDisable(uneditable);
    }

    @Override
    public void clean() {
        this.field = null;
        this.parentObj = null;
        this.workModeChangeListener = null;

        normalDisplay();
    }

    /** 将字段的值更新到controller界面*/
//    protected abstract void updateDisplay() throws Exception;

    /** 将界面的值更新到字段*/
//    protected abstract void updateValue() throws Exception;

    // 释放内存资源
    public void release() {
        if(!this.getChildren().isEmpty())
            this.getChildren().removeAll(this.getChildren());
    }

    protected void normalDisplay() {
        state = ControllerState.NORMAL;
    }

    protected void abnormalInput(){
        state = ControllerState.INPUT_ABNORMAL;
    }

    protected void abnormalReport() {
        state = ControllerState.REPORT_ABNORMAL;
    }

}
