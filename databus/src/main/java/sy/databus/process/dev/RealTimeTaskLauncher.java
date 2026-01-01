package sy.databus.process.dev;

import javafx.beans.value.ChangeListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.entity.STask;
import sy.databus.entity.signal.DATA_TASK_BEGIN;
import sy.databus.entity.signal.DATA_TASK_END;
import sy.databus.global.ProcessorType;
import sy.databus.global.WorkMode;
import sy.databus.organize.TaskManager;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.organize.monitor.WatchPaneShifter;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.Processor;
import sy.databus.view.watch.RealTimeTaskLauncherWatchPane;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static sy.databus.process.AbstractIntegratedProcessor.RoutingPattern.ALWAYS_TRANSITIVE;

@Log4j2
@Processor(
        type = ProcessorType.FRAME,
        pane = RealTimeTaskLauncherWatchPane.class
)
public class RealTimeTaskLauncher extends AbstractIntegratedProcessor {

    protected final DateFormat dateFormat = new SimpleDateFormat("HH_mm_ss_SSS", Locale.CHINA);

    @Getter
    private STask crntTask;

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {
        fireParsingEnd();
    }

    @Override
    protected AbstractInfoReporter createInfoReporter() {
        return new WatchPaneShifter(this);
    }

    @Override
    public boolean validateAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        return false;
    }

    public void fireParsingStart() {
        fireParsingStart(null);
    }

    public void fireParsingStart(ChangeListener<? super Boolean> listener) {
        try {
            crntTask = new STask(processorId);
            crntTask.addCompletionListener(listener);
            handleSigWithinExecutor(new DATA_TASK_BEGIN(crntTask
                    , dateFormat.format(CachedClock.instance().currentTimeMillis())
                    , this
                    , ALWAYS_TRANSITIVE)
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void fireParsingEnd() {
        if (crntTask == null) {
            return;
        }
        try {
            handleSigWithinExecutor(new DATA_TASK_END(this, crntTask));
        } catch (Exception e) {
            log.error(e.getMessage());
            TaskManager.forcefullyCancel(crntTask);
        }
    }

}
