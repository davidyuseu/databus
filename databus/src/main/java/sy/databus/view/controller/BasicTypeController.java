package sy.databus.view.controller;

import sy.databus.process.Console;

import java.lang.reflect.Field;

/**
 * 基本数据类型的控制器，在构造初始置入ControllerType.BASIC
 * */
public abstract class BasicTypeController extends ConsoleController{

    public BasicTypeController(Console console, Field field, Object obj) {
        super(console, field, obj);
    }

}
