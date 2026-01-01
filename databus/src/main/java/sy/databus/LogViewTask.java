package sy.databus;

import javafx.application.Platform;
import lombok.extern.log4j.Log4j2;
import sy.databus.view.logger.LogBuilder;
import sy.databus.view.logger.LogView;

import java.io.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Log4j2
public class LogViewTask extends UITask {

    public static final String mainLogPath = ".\\log\\MainLog.log";

    private BufferedReader logReader = null;

    private LogBuilder logBuilder;

    private final LogView logView;

    private final Object logViewLocker = new Object();

    private boolean first = true;

    public LogViewTask(LogView vLog) {
        this.logView = vLog;
        logBuilder = new LogBuilder(LogView.ROW_MAX_COUNT);
        task = () -> {
            String line;
            boolean updated = false, notMax;
            try {
                while ((line = logReader.readLine()) != null) {
                    synchronized (logViewLocker) {
                        notMax = logBuilder.addLineIfNotMax(line);
                    }
                    updated = updated | notMax;
                    if (!notMax)
                        break;
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            if (updated) {
                Platform.runLater(() -> {
                    synchronized (logViewLocker) {
                        this.logView.addFirstLogLine(logBuilder);
                    }
                });
            }
        };
    }

    @Override
    public void start(ScheduledThreadPoolExecutor executor, long initialDelay, long period, TimeUnit unit) {
        if (logReader == null) {
            File logFile = new File(mainLogPath);
            if (!logFile.getParentFile().exists()) {
                if (logFile.getParentFile().mkdirs()) {
                    if (!logFile.exists()) {
                        try {
                            logFile.createNewFile();
                        } catch (IOException e) {
                            log.error(e.getMessage() + "(Failed to create a main log!)");
                            return;
                        }
                    }
                } else {
                    log.error("Failed to create a log dir!");
                    return;
                }
            }
            try {
                logReader = new BufferedReader(new FileReader(mainLogPath));
                // 剔除每次启动时产生的若干行无用日志
                if (first) {
                    int discard = 0;
                    do {
                        if (logReader.readLine() == null)
                            break;
                        ++discard;
                    } while (discard < 4);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                return;
            }
        }
        super.start(executor, initialDelay, period, unit);
    }

    @Override
    public void cancel(boolean mayInterruptIfRunning) {
        super.cancel(mayInterruptIfRunning);
        try {
            logReader.close();
            logReader = null;
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }
}
