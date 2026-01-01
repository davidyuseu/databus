package sy.databus.entity;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import sy.databus.organize.TaskManager;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class STask {

    @FunctionalInterface
    public interface LoopLockedTask {
        void complete();
    }
    // 任务标识
    @Getter
    protected final UUID taskId;
    // 任务发起者
    @Getter
    protected final ProcessorId initiator;

    // 末端闭环计数
    @Getter
    protected final CountDownLatch loopLockedCountDown;

    // 任务闭环时要做的事
    @Getter @Setter
    protected LoopLockedTask closedLoop = null;

    // 任务闭环时是否做事
    @Getter @Setter
    protected boolean doClosedLoop = true;

    /* 可利用该标识通知UI界面*/
    @Getter @Setter
    protected SimpleBooleanProperty completionFlag = new SimpleBooleanProperty(false);
    public void switchCompletionFlag(boolean flag) {
        completionFlag.set(flag);
    }
    public void addCompletionListener(ChangeListener<? super Boolean> listener) {
        if (listener != null)
            completionFlag.addListener(listener);
    }

    @SneakyThrows
    public STask (UUID taskId, ProcessorId initiator) {
        this.taskId = taskId;
        this.initiator = initiator;
        /** STask应该仅在分析模式下发起创建，否则{@link TaskManager.endingsNumberMap}在编辑模式下是空的*/
        Integer loopLockedCount = TaskManager.endingsNumberMap.get(this.initiator);
        if (loopLockedCount == null) {
            loopLockedCount = TaskManager.acquireLoopLockedNumber(this.initiator.getOwner());
            TaskManager.endingsNumberMap.put(this.initiator, loopLockedCount);
        }
        this.loopLockedCountDown = new CountDownLatch(loopLockedCount);
    }

    public STask (UUID taskId, ProcessorId initiator, LoopLockedTask closedLoop) {
        this(taskId, initiator);
        this.closedLoop = closedLoop;
    }

    public STask (ProcessorId initiator) {
        this(UUID.randomUUID(), initiator);
    }

    public STask (ProcessorId initiator, LoopLockedTask closedLoop) {
        this(UUID.randomUUID(), initiator);
        this.closedLoop = closedLoop;
    }

}
