package sy.databus.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.UUID;

@Log4j2
public class ReplayFileItem {
    @Getter @Setter
    private int num;
    @Getter @Setter
    private String name; // fileName
    @Getter @Setter
    private File file;

    @Getter @Setter
    private RelatedInfo relatedInfo;

    @Getter
    private final UUID uuid;
    @Getter @Setter
    private String startTime = "";
    @Getter @Setter
    private String endTime = "";

    public void setTaskTip(String tip) {
        if (relatedInfo != null) {
            relatedInfo.getTaskTip().set(tip);
        } else {
            log.error("The task of {} did not got a 'taskInfo'!", name);
        }
    }

    public void setTaskTitle(String title) {
        if (relatedInfo != null) {
            relatedInfo.getTaskTitle().set(title);
        } else {
            log.error("The task of {} did not got a 'taskInfo'!", name);
        }
    }

    public ReplayFileItem(int num, String name, File file) {
        this.num = num;
        this.name = name;
        this.file = file;

        this.uuid = UUID.randomUUID();
    }

    public ReplayFileItem(int num, String name, File file, RelatedInfo relatedInfo) {
        this(num, name, file);
        this.relatedInfo = relatedInfo;
    }
}
