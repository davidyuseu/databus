package sy.databus.entity.property;

import java.util.Objects;

public class StringSafeProperty implements IProperty<String> {
    String value;

    public StringSafeProperty(String value) {
        this.value = value;
    }

    @Override
    public synchronized String getValue() {
        return value;
    }

    @Override
    public synchronized void setValue(String s) {
        this.value = s;
    }

    @Override
    public <K> void setValueByInput(K input) throws Exception{
        Objects.requireNonNull(input);
        if(!(input instanceof String))
            throw new PropertyException("The actual parameter of the method 'setValueByInput' must be of type String");
        else {
            this.value = (String) input;
        }
    }
}
