package sy.databus.view.controller;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.extern.log4j.Log4j2;
import sy.databus.process.Console;
import sy.databus.view.SLabel;

import java.lang.reflect.Field;


@Log4j2
public abstract class AbstractTextFieldController extends ConsoleController implements IDataModifier, IDataVisualization {

    protected VBox vBox = new VBox();

    protected SLabel label = new SLabel();

    protected TextField textField = new TextField();

    public AbstractTextFieldController(Console console, Field field, Object obj) {
        super(console, field, obj);

        if (!console.display().isEmpty())
            label.setText(console.display());
        else
            label.setText(field.getName());


        textField.setPrefWidth(238d);
//        textField.setDisable(!editable);

        vBox.getChildren().addAll(label, textField);
        vBox.setSpacing(4d);
//        vBox.setLayoutX(21d);

        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue){
                if(textField.getText() == null || textField.getText().equals(""))
                    return;
                try {
                    updateValue();
                } catch (NullPointerException eNull) {
                    abnormalInput();
                    log.warn("{} 的输入为空！", annoConsole.display());
                    return;
                } catch (Exception e) {
                    abnormalInput();
                    log.warn(e);
                    return;
                }
                normalDisplay();
            }
        });

        this.getChildren().add(vBox);
    }

    @Override
    public void rebuild(Console console, Field field, Object obj) {
        super.rebuild(console, field, obj);
        if (!console.display().isEmpty())
            label.setText(console.display());
        else
            label.setText(field.getName());

        setUneditable(!editable);
    }

    @Override
    public void setUneditable(boolean uneditable) {
        editable = !uneditable;
        textField.setDisable(uneditable);
    }

    @Override
    protected void normalDisplay() {
        super.normalDisplay();
        label.setNormalDisplay();
        textField.setStyle("-fx-border-color: transparent");
    }

    @Override
    protected void abnormalInput(){
        super.abnormalInput();
        label.setErrorDisplay();
        textField.setStyle("-fx-border-color: red");
    }

    @Override
    protected void abnormalReport() {
        super.abnormalReport();
        label.setWarnDisplay();
        textField.setStyle("-fx-border-color: #ff7700");
    }


    public void updateDisplay() {
        String report = "";
        try {
            report = getReport();
        } catch (NullPointerException eNull) {
            abnormalReport();
            return;
        } catch (Exception e) {
            abnormalReport();
            log.warn(e);
            return;
        }
        normalDisplay();
        textField.setText(report);
    }

    // 由字段获取回报的值
    protected abstract String getReport() throws Exception;

    @Override
    public void clean() {
        super.clean();
        textField.setText("");
    }
}
