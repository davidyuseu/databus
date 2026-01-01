package sy.databus.view.controller;

import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.common.cache.CacheFactory;
import sy.databus.process.Console;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class BasicCheckBoxController extends AbstractCheckBoxController{
    interface Initializer {
        void init(BasicCheckBoxController checkBoxController, Field field, Object obj) throws Exception;
    }

    enum DataFormattingStrategy {
        BOOLEAN (boolean.class) {
            @Override
            public void format(Field field, Object obj, boolean var) throws Exception {
                field.set(obj, var);
            }

            @Override
            public boolean show(Field field, Object obj) throws Exception {
                return (boolean) field.get(obj);
            }
        },

        ATOMIC_BOOLEAN (AtomicBoolean.class) {
            @Override
            public void format(Field field, Object obj, boolean var) throws Exception {
                AtomicBoolean value = (AtomicBoolean) field.get(obj);
                if (value == null)
                    field.set(obj, new AtomicBoolean(var));
                else
                    value.set(var);
            }

            @Override
            public boolean show(Field field, Object obj) throws Exception {
                AtomicBoolean value = (AtomicBoolean) field.get(obj);
                return value.get();
            }
        },

        BOOLEAN_SIMPLE_PROPERTY (SimpleBooleanProperty.class
//                ,(checkBoxController, field, obj) -> {
//                    SimpleBooleanProperty value = (SimpleBooleanProperty) field.get(obj);
//                    value.bind(checkBoxController.getCheckBox().selectedProperty());
//                }
        ) {
            @Override
            public void format(Field field, Object obj, boolean var) throws Exception {
                SimpleBooleanProperty value = (SimpleBooleanProperty) field.get(obj);
                if (value == null)
                    field.set(obj, new SimpleBooleanProperty(var));
                else
                    value.set(var);
            }

            @Override
            public boolean show(Field field, Object obj) throws Exception {
                SimpleBooleanProperty value = (SimpleBooleanProperty) field.get(obj);
                return value.get();
            }
        };

        @Getter
        private Type fieldType;
        @Getter
        private Initializer initializer = null;

        DataFormattingStrategy (Type fieldType) {
            this.fieldType = fieldType;
        }

        DataFormattingStrategy (Type fieldType, Initializer initializer) {
            this.fieldType = fieldType;
            this.initializer = initializer;
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
        public abstract void format(Field field, Object obj, boolean var) throws Exception;

        /** 将field的值转换为String，供controller展示*/
        public abstract boolean show(Field field, Object obj) throws Exception;

    }

    // 数据格式化策略
    private DataFormattingStrategy dataFormatter;

    public static BasicCheckBoxController buildController(Type fieldType, Console console, Field field, Object obj) {
        DataFormattingStrategy strategy = DataFormattingStrategy.getStrategy(fieldType);
        if (strategy != null) {
            field.setAccessible(true);
            BasicCheckBoxController cachedController
                    = (BasicCheckBoxController) CacheFactory.getWeakCache(BasicCheckBoxController.class);
            if (cachedController != null) {
                cachedController.rebuild(console, field, obj, strategy);
                return cachedController;
            } else {
                return new BasicCheckBoxController(console, field, obj, strategy);
            }
        }else return null;
    }

    @SneakyThrows
    public BasicCheckBoxController(Console console, Field field, Object obj, DataFormattingStrategy dataFormatter) {
        super(console, field, obj);

        this.dataFormatter = dataFormatter;
        Initializer initializer = this.dataFormatter.initializer;
        if(initializer != null)
            initializer.init(this, this.field, this.parentObj); // @SneakyThrows

        updateDisplay();
    }

    @SneakyThrows
    public void rebuild(Console console, Field field, Object obj, DataFormattingStrategy dataFormatter) {
        super.rebuild(console, field, obj);

        this.dataFormatter = dataFormatter;
        Initializer initializer = this.dataFormatter.initializer;
        if(initializer != null)
            initializer.init(this, this.field, this.parentObj); // @SneakyThrows

        updateDisplay();
    }

    @Override
    protected boolean getReport() throws Exception {
        return dataFormatter.show(field,parentObj);
    }

    @Override
    public void updateValue() throws Exception {
        boolean display = checkBox.isSelected();
        dataFormatter.format(field, parentObj, display);
    }
}
