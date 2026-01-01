package sy.databus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UISchedules {
    // 定时刷新控制台界面的线程需要和反射创建控制台界面以及删除控制台界面组件的线程持有同一把锁；
    public static final Object consoleUILock = new Object();

    private static final ScheduledThreadPoolExecutor uiScheduledExecutor
            = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(4);

    private static final ConsoleRefreshTask consoleRefreshTask = new ConsoleRefreshTask();
    private static final WatchersRefreshTask watchersRefreshTask = new WatchersRefreshTask();
    private static final OriDataViewTask oriDataViewTask
            = new OriDataViewTask(MainPaneController.INSTANCE.getOriDataViewController());
    private static final LogViewTask logViewTask
            = new LogViewTask(MainPaneController.INSTANCE.getLogView());

    public static void initUIExecutors() {
        uiScheduledExecutor.setRemoveOnCancelPolicy(true);
    }

    public static void startUIScheduledTasks() {
        if (consoleRefreshTask.isStopped()) {
            consoleRefreshTask.start(uiScheduledExecutor, 0,
                    1000, TimeUnit.MILLISECONDS);
        }

        if (watchersRefreshTask.isStopped()) {
            watchersRefreshTask.start(uiScheduledExecutor, 0,
                    1000, TimeUnit.MILLISECONDS);
        }
    }

    public static void stopUIScheduledTasks() {
        if (!consoleRefreshTask.isStopped()) {
            consoleRefreshTask.cancel(false);
        }

        if (!watchersRefreshTask.isStopped()) {
            watchersRefreshTask.cancel(false);
        }
    }

    public static boolean startOriDataViewTimers() {
        if (oriDataViewTask.isStopped()) {
            oriDataViewTask.start(uiScheduledExecutor, 0,
                    80, TimeUnit.MILLISECONDS);
            MainPaneController.INSTANCE.getOriDataViewController()
                    .toggleSwitchBtn(true);
            return true;
        } else {
            return false;
        }
    }

    public static boolean stopOriDataViewTimers() {
        if (!oriDataViewTask.isStopped()) {
            oriDataViewTask.cancel(false);
            MainPaneController.INSTANCE.getOriDataViewController()
                    .toggleSwitchBtn(false);
            return true;
        } else {
            return false;
        }
    }

    public static boolean startLogViewTimer() {
        if (logViewTask.isStopped()) {
            logViewTask.start(uiScheduledExecutor, 0,
                    1000, TimeUnit.MILLISECONDS);
            return true;
        } else {
            return false;
        }
    }

    public static boolean stopLogViewTimer() {
        if (!logViewTask.isStopped()) {
            logViewTask.cancel(false);
            return true;
        } else {
            return false;
        }
    }

    public static void activeMonitoring() {
        watchersRefreshTask.setMonitorActive(true);
    }

    public static void blockMonitoring() {
        watchersRefreshTask.setMonitorActive(false);
    }

}
