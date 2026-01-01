package sy.databus.view.monitor;

import sy.databus.organize.monitor.AbstractInfoReporter;

public interface MonitorTab<T extends AbstractInfoReporter> {

    void addItem(T reporter);

    void removeItem(T reporter);

    void clearItems();

    void refreshItems();
}
