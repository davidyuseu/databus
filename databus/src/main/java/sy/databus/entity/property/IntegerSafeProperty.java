package sy.databus.entity.property;

import java.util.Objects;

/**
 * ps: 搜帧类一般不适用动态属性，即在任务模式下，不在控制台中去设置搜帧类的IProperty属性
 * */
public class IntegerSafeProperty implements IProperty<Integer> {
    private int value;

    public IntegerSafeProperty(Integer value) {
        this.value = value;
    }

    @Override
    public synchronized Integer getValue() {
        return value;
    }

    @Override
    public synchronized void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public synchronized <K> void setValueByInput(K input) throws Exception{
        Objects.requireNonNull(input);
        if(input instanceof String) {
            String str = (String) input;
            value = Integer.valueOf(str);
        }else if(input instanceof Integer){
            value = (Integer) input;
        }else throw new PropertyException("The actual parameter of the method 'setValueByInput' must be of type String or Integer");
    }

}
