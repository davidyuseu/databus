package sy.databus.view.controller;

import javafx.scene.control.CheckBox;
import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import sy.databus.process.Console;

import java.lang.reflect.Field;

import static sy.databus.global.Constants.COLOR_ITEM_NAME;

@Log4j2
public abstract class AbstractCheckBoxController extends ConsoleController implements IDataModifier, IDataVisualization {

    @Getter
    protected CheckBox checkBox = new CheckBox();

    public AbstractCheckBoxController(Console console, Field field, Object obj) {
        super(console, field, obj);

        if (!console.display().isEmpty())
            checkBox.setText(console.display());
        else
            checkBox.setText(field.getName());
        checkBox.getStyleClass().add("controllerStyle0");

        checkBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1 != null) {
                try {
                    updateValue();
                } catch (Exception e) {
                    // #- log to view
                    abnormalInput();
                    log.warn(e);
                    return;
                }
                normalDisplay();
            }
        });

        this.getChildren().add(checkBox);
    }

    public void rebuild(Console console, Field field, Object obj) {
        super.rebuild(console, field, obj);
        if (!console.display().isEmpty())
            checkBox.setText(console.display());
        else
            checkBox.setText(field.getName());

        setUneditable(!editable);
    }

    @Override
    public void setUneditable(boolean uneditable) {
        editable = !uneditable;
        checkBox.setDisable(uneditable);
    }

    @Override
    protected void normalDisplay() {
        super.normalDisplay();
        checkBox.setTextFill(Paint.valueOf(COLOR_ITEM_NAME));
    }

    @Override
    protected void abnormalInput(){
        super.abnormalInput();
        checkBox.setTextFill(Paint.valueOf("#FF0000FF"));
    }

    @Override
    protected void abnormalReport() {
        super.abnormalReport();
        checkBox.setTextFill(Paint.valueOf("#FF7700FF"));
    }

    public void updateDisplay() {
        boolean report = false;
        try {
            report = getReport();
        } catch (Exception e) {
            abnormalReport();
            log.warn(e);
            return;
        }
        normalDisplay();
        checkBox.setSelected(report);
    }

    /** 由字段获取回报的值*/
    protected abstract boolean getReport() throws Exception;

    @Override
    public void clean() {
        super.clean();
//        checkBox.setSelected(false); // field等对象已经在super.clean中释放，此时再触发selected事件会在updateValue()中产生空指针异常
//        checkBox.selectedProperty().unbind(); // 解除绑定数据
    }

}
