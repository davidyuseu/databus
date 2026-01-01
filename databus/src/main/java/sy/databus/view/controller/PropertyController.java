package sy.databus.view.controller;

import sy.databus.process.Console;

import java.lang.reflect.Field;


/**
 * IProperty数据类型的控制器，在构造初始置入ControllerType.BASIC
 * */
public abstract class PropertyController extends ConsoleController {

    public PropertyController(Console console, Field field, Object obj) {
        super(console, field, obj);
    }

}
