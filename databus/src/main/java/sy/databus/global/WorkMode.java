package sy.databus.global;

public enum WorkMode {
    ORIGINAL("原始状态"), // 开机状态
    EDIT("编辑模式"),
    MISSION("任务模式"),
    ANALYSIS("分析模式");

    String description;

    WorkMode(String description) {
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
