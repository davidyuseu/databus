package sy.databus.view.controller;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import sy.common.cache.CacheFactory;
import sy.common.util.SByteUtil;
import sy.databus.entity.property.SimpleStrProperty;
import sy.databus.process.Console;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@Log4j2
public class BasicInputController extends AbstractTextFieldController {

    enum DataFormattingStrategy {
        INTEGER (int.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                field.set(parentObj, Integer.parseInt(str));
            }
        },

        OBJ_INTEGER (Integer.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                field.set(parentObj, Integer.parseInt(str));
            }
        },

        ATOMIC_INTEGER (AtomicInteger.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                int tVar = Integer.parseInt(str);
                AtomicInteger value = (AtomicInteger) field.get(parentObj);
                if (value == null)
                    field.set(parentObj, new AtomicInteger(tVar));
                else
                    value.set(tVar);
            }
        },

        INTEGER_SIMPLE_PROPERTY (SimpleIntegerProperty.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                int tVar = Integer.parseInt(str);
                SimpleIntegerProperty value = (SimpleIntegerProperty) field.get(parentObj);
                if (value == null)
                    field.set(parentObj, new SimpleIntegerProperty(tVar));
                else
                    value.set(tVar);
            }

            @Override
            public String show(Field field, Object parentObj) throws Exception{
                SimpleIntegerProperty value = (SimpleIntegerProperty) field.get(parentObj);
                return String.valueOf(value.get());
            }

        },

        BYTE (byte.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                field.set(parentObj, Byte.parseByte(str, 16));
            }

            @Override
            public String show(Field field, Object parentObj) throws Exception{
                byte value = (byte) field.get(parentObj);
                return Integer.toHexString(value);
            }
        },

        SHORT (short.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                field.set(parentObj, Short.parseShort(str));
            }
        },

        LONG (long.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                field.set(parentObj, Long.parseLong(str));
            }
        },

        LONG_SIMPLE_PROPERTY (SimpleLongProperty.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                long tVar = Long.parseLong(str);
                SimpleLongProperty value = (SimpleLongProperty) field.get(parentObj);
                if (value == null)
                    field.set(parentObj, new SimpleLongProperty(tVar));
                else
                    value.set(tVar);
            }

            @Override
            public String show(Field field, Object parentObj) throws Exception{
                SimpleLongProperty value = (SimpleLongProperty) field.get(parentObj);
                return String.valueOf(value.get());
            }
        },

        ATOMIC_LONG (AtomicLong.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                long tVar = Long.parseLong(str);
                AtomicLong value = (AtomicLong) field.get(parentObj);
                if (value == null)
                    field.set(parentObj, new AtomicLong(tVar));
                else
                    value.set(tVar);
            }
        },

        FLOAT (float.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                field.set(parentObj, Float.parseFloat(str));
            }
        },

        DOUBLE (double.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                field.set(parentObj, Double.parseDouble(str));
            }
        },

        DOUBLE_SIMPLE_PROPERTY (SimpleDoubleProperty.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                double tVar = Double.parseDouble(str);
                SimpleDoubleProperty value = (SimpleDoubleProperty) field.get(parentObj);
                if (value == null)
                    field.set(parentObj, new SimpleDoubleProperty(tVar));
                else
                    value.set(tVar);
            }

            @Override
            public String show(Field field, Object parentObj) throws Exception{
                SimpleDoubleProperty value = (SimpleDoubleProperty) field.get(parentObj);
                return String.valueOf(value.get());
            }
        },

        STRING (String.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                field.set(parentObj, str);
            }

            @Override
            public String show(Field field, Object parentObj) throws Exception{
                return (String) field.get(parentObj);
            }

        },

        STRING_SIMPLE_PROPERTY (SimpleStringProperty.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                SimpleStringProperty value = (SimpleStringProperty) field.get(parentObj);
                if (value == null)
                    field.set(parentObj, new SimpleStringProperty(str));
                else
                    value.set(str);
            }

            @Override
            public String show(Field field, Object parentObj) throws Exception{
                SimpleStringProperty value = (SimpleStringProperty) field.get(parentObj);
                return value.get();
            }
        },

        STR_SIMPLE_PROPERTY (SimpleStrProperty.class) {
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                STRING_SIMPLE_PROPERTY.format(field, parentObj, str);
            }
        },

        BYTEBUF (ByteBuf.class) { // !STATIC
            @Override
            public void format(Field field, Object parentObj, String str) throws Exception {
                ByteBuf value = (ByteBuf) field.get(parentObj);
                if (value != null) { // 先释放原内存
                    if (value.refCnt() > 0)
                        value.release(value.refCnt());
                }
                field.set(parentObj, SByteUtil.getBufByString(str));
            }

            @Override
            public String show(Field field, Object parentObj) throws Exception{
                ByteBuf value = (ByteBuf) field.get(parentObj);
                byte[] bytes = new byte[value.writerIndex()];
                value.getBytes(0, bytes);
                return StringUtil.toHexStringPadded(bytes);
            }
        }
        ;

        @Getter
        private final Type fieldType;

        DataFormattingStrategy (Type fieldType) {
            this.fieldType = fieldType;
        }

        public static DataFormattingStrategy getStrategy(Type fieldType) {
            for (DataFormattingStrategy strategy : DataFormattingStrategy.values()) {
                if (strategy.getFieldType().equals(fieldType)) {
                    return strategy;
                }
            }
            return null;
        }

        /** 将Controller界面配置的值置入其对应的field中*/
        public abstract void format(Field field, Object parentObj, String str) throws Exception;

        /** 将field的值转换为String，供controller展示*/
        public String show(Field field, Object parentObj) throws Exception{
            return String.valueOf(field.get(parentObj));
        }

    }

    // 数据格式化策略
    private DataFormattingStrategy dataFormatter;

    public static BasicInputController buildController(Type fieldType, Console console, Field field, Object obj) {
        DataFormattingStrategy strategy = DataFormattingStrategy.getStrategy(fieldType);
        if (strategy != null) {
            field.setAccessible(true);
            BasicInputController cachedController
                    = (BasicInputController) CacheFactory.getWeakCache(BasicInputController.class);
            if (cachedController != null) { // 拿到缓存，自行重构对象
                cachedController.rebuild(console, field, obj, strategy);
                return cachedController;
            } else {
                return new BasicInputController(console, field, obj, strategy);
            }
        }else return null;
    }

    public BasicInputController(Console console, Field field, Object obj, DataFormattingStrategy dataFormatter) {
        super(console, field, obj);

        this.dataFormatter = dataFormatter;

        updateDisplay();
    }

    public void rebuild(Console console, Field field, Object obj, DataFormattingStrategy dataFormatter) {
        super.rebuild(console, field, obj);

        this.dataFormatter = dataFormatter;

        updateDisplay();
    }

    @Override
    protected String getReport() throws Exception {
        return dataFormatter.show(field, parentObj);
    }

    @Override
    public void updateValue() throws Exception {
        String display = textField.getText().trim();
        var reg = annoConsole.reg();
        if (!reg.isEmpty()) {
            if (Pattern.compile(reg).matcher(display).matches()) {
                dataFormatter.format(field, parentObj, display);
            } else throw new Exception("输入格式有误！");
        } else {
            dataFormatter.format(field, parentObj, display);
        }
    }

}
