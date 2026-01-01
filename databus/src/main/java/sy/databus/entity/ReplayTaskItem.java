package sy.databus.entity;

import lombok.Getter;

import java.sql.Timestamp;
import java.util.UUID;

public class ReplayTaskItem {

    @Getter
    private int num;
    @Getter
    private UUID taskUid;
    @Getter
    private String processingId;
    @Getter
    private String projectName;
    @Getter
    private String taskTable;
    @Getter
    private Timestamp createdAt;

    public ReplayTaskItem(int num,
                          UUID taskUid,
                          String processingId,
                          String projectName,
                          String taskTable,
                          Timestamp createdAt) {
        this.num = num;
        this.taskUid = taskUid;
        this.processingId = processingId;
        this.projectName = projectName;
        this.taskTable = taskTable;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ReplayTaskItem{" +
                "num=" + num +
                ", task_uid=" + taskUid +
                ", processing_id='" + processingId + '\'' +
                ", project_name='" + projectName + '\'' +
                ", task_table='" + taskTable + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
