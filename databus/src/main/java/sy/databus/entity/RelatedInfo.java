package sy.databus.entity;

import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

public class RelatedInfo {

    @Getter
    private SimpleStringProperty taskTip = new SimpleStringProperty();
    public void setTaskTip(String tipValue) {
        taskTip.set(tipValue);
    }
    @Getter
    private SimpleStringProperty taskTitle = new SimpleStringProperty();
    public void setTaskTitle(String titleValue) {
        taskTitle.set(titleValue);
    }
}
