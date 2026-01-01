package sy.databus.entity.property;

/**
 * 子类实现方法时请添加synchronized关键字，以确保线程安全
 * T 标识实现类中value的类型
 * K 标识{@link #setValueByInput(Object input)}方法通过何种类型的入参来将input转换为value
 */
public interface IProperty<T> {
     T getValue() throws Exception;

     void setValue(T t) throws Exception;

     <K> void setValueByInput(K input) throws Exception;

}
