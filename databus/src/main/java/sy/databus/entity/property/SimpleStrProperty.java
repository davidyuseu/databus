package sy.databus.entity.property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.SimpleStringProperty;

@JsonIgnoreProperties(ignoreUnknown = true,
        value = {"bean","name"})
public class SimpleStrProperty extends SimpleStringProperty {

    public SimpleStrProperty(String str) {
        super(str);
    }

    @Override
    public String toString() {
        return this.get();
    }
}
