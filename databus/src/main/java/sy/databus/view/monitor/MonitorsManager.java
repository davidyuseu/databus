package sy.databus.view.monitor;

import javafx.scene.control.TabPane;
import sy.databus.organize.ProcessorManager;
import sy.databus.organize.monitor.MonitorGroup;

import java.util.HashMap;
import java.util.Map;

import static sy.databus.organize.monitor.MonitorGroup.*;


public class MonitorsManager {

    private Map<MonitorGroup, MonitorTab> monitorTabs = new HashMap<>();

    private TabPane container = null;

    public MonitorsManager(TabPane container) {
        this.container = container;
    }

    public synchronized void clearAllMonitorTabs() {
        container.getTabs().clear();
        for (var entry : monitorTabs.entrySet()) {
            entry.getValue().clearItems();
        }
        monitorTabs.clear();
    }

    public synchronized void shiftTheControls() {
        var tab = (WatchPaneShifterTab) monitorTabs.get(CONTROL);
        if (tab != null)
            tab.shiftItems();
    }

    public synchronized void putBackTheControls() {
        var tab = (WatchPaneShifterTab) monitorTabs.get(CONTROL);
        if (tab != null)
            tab.putBackItems();
    }

    public synchronized void refreshAllMonitorTabs() {
        for (var entry : monitorTabs.entrySet()) {
            entry.getValue().refreshItems();
        }
    }

    public void generateMonitorTabs() {
        for (var processor : ProcessorManager.getAllProcessor()) {
            if (!processor.isToReportInfo())
                continue;

            switch (processor.getInfoReporter().getGroup()) {
                case FRAME_PROC -> {
                    MonitorTab tab = monitorTabs.get(FRAME_PROC);
                    if (tab == null) {
                        tab = new FrameProcTab();
                        container.getTabs().add((FrameProcTab) tab);
                        monitorTabs.put(FRAME_PROC, tab);
                    }
                    tab.addItem(processor.getInfoReporter());
                }
                case CONTROL -> {
                    MonitorTab tab = monitorTabs.get(CONTROL);
                    if (tab == null) {
                        tab = new WatchPaneShifterTab();
                        container.getTabs().add((WatchPaneShifterTab) tab);
                        monitorTabs.put(CONTROL, tab);
                    }
                    ((WatchPaneShifterTab) tab).recordItem(processor.getInfoReporter());
                }
                case IN_DEV -> {
                    MonitorTab tab = monitorTabs.get(IN_DEV);
                    if (tab == null) {
                        tab = new InDevTab();
                        container.getTabs().add((InDevTab) tab);
                        monitorTabs.put(IN_DEV, tab);
                    }
                    tab.addItem(processor.getInfoReporter());
                }
                case OUT_DEV -> {
                    MonitorTab tab = monitorTabs.get(OUT_DEV);
                    if (tab == null) {
                        tab = new OutDevTab();
                        container.getTabs().add((OutDevTab) tab);
                        monitorTabs.put(OUT_DEV, tab);
                    }
                    tab.addItem(processor.getInfoReporter());
                }
            }
        }
    }
}
