package sy.databus.global;

/**
 * 在ui配置界面域，显示的分组名称
 * */
public enum HandlerCategory {
    UNDEFINED("其它"),
    IPS_CHECKERS("Integrated Processor State Checkers");

    String display;

    HandlerCategory(String display) {
        this.display = display;
    }

    String getDisplay(){
        return display;
    }
}
