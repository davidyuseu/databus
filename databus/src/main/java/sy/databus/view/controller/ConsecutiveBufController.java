package sy.databus.view.controller;

import io.netty.util.internal.StringUtil;

import lombok.extern.log4j.Log4j2;
import sy.databus.entity.property.ConsecutiveBufSafeProperty;
import sy.common.cache.CacheFactory;
import sy.databus.process.Console;

import java.lang.reflect.Field;

@Log4j2
public class ConsecutiveBufController extends AbstractTextFieldController{

    private ConsecutiveBufSafeProperty property;

    public static ConsecutiveBufController buildController(Console console,
                                                           Field field,
                                                           Object obj,
                                                           ConsecutiveBufSafeProperty property) {
        ConsecutiveBufController cachedController
                = (ConsecutiveBufController) CacheFactory.getWeakCache(ConsecutiveBufController.class);
        if (cachedController != null) {
            cachedController.rebuild(console, field, obj, property);
            return cachedController;
        } else {
            return new ConsecutiveBufController(console, field, obj, property);
        }
    }

    public ConsecutiveBufController(Console console, Field field, Object obj, ConsecutiveBufSafeProperty property) {
        super(console, field, obj);
        this.property = property;

        updateDisplay();
    }

    public void rebuild(Console console, Field field, Object obj, ConsecutiveBufSafeProperty property) {
        super.rebuild(console, field, obj);
        this.property = property;

        updateDisplay();
    }

    @Override
    protected String getReport() throws Exception {
        if (property != null)
            return StringUtil.toHexStringPadded(property.getValue().array());
        else throw new Exception("null property!");
    }

    @Override
    public void updateValue() throws Exception {
        String display = textField.getText().trim();
        if (property != null)
            property.setValueByInput(display);
        else
            property = new ConsecutiveBufSafeProperty(display);
    }

}
