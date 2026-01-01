package sy.databus.organize;


import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.databus.entity.ProcessorId;
import sy.databus.entity.STask;
import sy.databus.entity.signal.CLEAN_PIPELINE;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.IRouter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Log4j2
public class TaskManager {

    private static final Object mutex = new Object();

    private static boolean bFlag = true;

    /** 切换至工作模式时启动*/
    public static void start() {
        synchronized (mutex) {
            bFlag = true;
        }
    }

    // 每个任务发起者对应的末端总数
    public static Map<ProcessorId, Integer> endingsNumberMap = new ConcurrentHashMap<>();

    // 任务清单
    private static List<STask> tasks = new LinkedList<>();

    public static boolean addTask(STask task) {
        synchronized (mutex) {
            if (!bFlag) {
                log.warn("TaskManager has closed, reject to add a new task!");
                return false;
            }
            return tasks.add(task);
        }
    }

    public static boolean isCompleted(UUID taskId) {
        synchronized (mutex) {
            for (STask task : tasks) {
                if (task.getTaskId().equals(taskId))
                    return false;
            }
            return true;
        }
    }

    // 迭代获取任务发起者末端闭环次数
    public static int acquireLoopLockedNumber(IRouter initiator) {
        Set<IRouter> ends = new HashSet<>();
        acquireLoopLockedNumber0(initiator, ends);
        return ends.size();
    }

    private static void acquireLoopLockedNumber0(IRouter processor, Set<IRouter> ends) {
        List<IRouter> nextProcs = processor.getNextProcessors();
        if (nextProcs == null || nextProcs.isEmpty()) { // 到达末端
            ends.add(processor);
        } else {
            for (IRouter next : nextProcs) {
                acquireLoopLockedNumber0(next, ends);
            }
        }
    }

    @Data
    private static class LoopLockedCount {
        int count;
        public void increase() {
            count++;
        }
    }

    // 迭代获取两个节点之间任务闭环次数
    public static int acquireLoopNumFrom(IRouter initiator, IRouter ending) {
        if (initiator == ending) // source和ending相同时返回触发条件次数为1(为0表示不可达)
            return 1;
        LoopLockedCount loopCount = new LoopLockedCount();
        acquireLoopNumFrom0(initiator, ending, loopCount);
        return loopCount.count;
    }

    private static void acquireLoopNumFrom0(IRouter start,
                                            IRouter ending,
                                            LoopLockedCount loopCount) {
        List<IRouter> nextProcs = start.getNextProcessors();
        if (nextProcs == null || nextProcs.isEmpty()) // 到达末端
            return;
        if (nextProcs.contains(ending)) {
            loopCount.increase();
        } else {
            for (IRouter next : nextProcs) {
                acquireLoopNumFrom0(next, ending, loopCount);
            }
        }
    }

    public static void closeLoop(STask task) {
        synchronized (task) { // 防止同一个任务被不同尾处理器闭环多次
            if (task.getLoopLockedCountDown().getCount() == 1) {
                synchronized (mutex) { // 同步任务清单移除当前任务的操作
                    if (task.getClosedLoop() != null && task.isDoClosedLoop())
                        task.getClosedLoop().complete();
                    task.switchCompletionFlag(true);
                    tasks.remove(task);
                }
            }
            task.getLoopLockedCountDown().countDown();
        }
    }

    public static void forcefullyCancel(STask task) {
        synchronized (task) {
            synchronized (mutex) {
                tasks.remove(task);
            }
        }
    }

    public static void waitFor(STask task) throws InterruptedException {
        CountDownLatch latch = task.getLoopLockedCountDown();
        if (latch != null)
            latch.await();
    }

    /** 调用该方法时确保不会再添加新的任务到tasks中 */
    @SneakyThrows
    public static void syncCloseTasks() {
        cleanPipelineFromSources();
        List<CountDownLatch> countDownLatches;
        synchronized (mutex) {
            countDownLatches = tasks.stream().map(STask::getLoopLockedCountDown)
                    .collect(Collectors.toList());
        }
        /** 由此可知，每个processor中的处理尽可能是同步的，
         * 或有异步操作也可以在执行{@link AbstractIntegratedProcessor#reset}方法后
         * 通过{@link AbstractIntegratedProcessor#closingFuture}同步掉，否则即使CLEAN_PIPELINE被各末端processor处理了，
         * 也可能存在正在执行的异步处理
         * 即每个processor尽可能处理消息和信号都是同步的*/
        for (CountDownLatch c : countDownLatches) {
            c.await();
        }

        ProcessorManager.clearSources();
        endingsNumberMap.clear();
        bFlag = false;
        if (tasks.size() > 0)
            log.error("The size of tasks should be zero after closing sync!");
    }

    public static void cleanPipelineFromSources() {
        for (AbstractIntegratedProcessor processor : ProcessorManager.getSourceProcessors()) {
            // 创建clean pipeline 任务
            STask cleanPipeline = new STask(processor.getProcessorId());
            try {
                processor.handleSigWithinExecutor(new CLEAN_PIPELINE(processor, cleanPipeline));
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
}
