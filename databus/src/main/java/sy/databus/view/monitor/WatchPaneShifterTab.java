package sy.databus.view.monitor;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.FlowPane;
import lombok.extern.log4j.Log4j2;
import sy.databus.MainPaneController;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.organize.monitor.WatchPaneShifter;
import sy.databus.view.customskins.titled.TitledNodeSkin;
import sy.databus.view.watch.WatchPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sy.databus.organize.monitor.MonitorGroup.CONTROL;

@Log4j2
public class WatchPaneShifterTab extends Tab implements MonitorTab {

    // 外层FlowPane
    private FlowPane outerPanel = new FlowPane();
    private ScrollPane scrollPane = new ScrollPane(outerPanel);

    private Map<Class<? extends WatchPane>, FlowPane> shifterMap = new HashMap();

    private List<WatchPaneShifter> shifterList = new ArrayList<>();

    public WatchPaneShifterTab() {
        super(CONTROL.getDisplay());
        scrollPane.setStyle("-fx-background-color: white");
        outerPanel.setPadding(new Insets(2d));
        outerPanel.prefWidthProperty().bind(scrollPane.widthProperty());
        this.setContent(scrollPane);
        this.setClosable(false);
    }

    public void recordItem(AbstractInfoReporter reporter) {
        if (reporter instanceof WatchPaneShifter watchPaneShifter) {
            shifterList.add(watchPaneShifter);
        } else {
            log.error("An Unsupported type of InfoReporter!");
        }
    }

    public void shiftItems() {
        for (var reporter : shifterList) {
            addItem(reporter);
        }
    }

    public void putBackItems() {
        for (var reporter : shifterList) {
            putBackItem(reporter);
        }
    }

    @Override
    public void addItem(AbstractInfoReporter reporter) {
        if (reporter instanceof WatchPaneShifter watchPaneShifter) {
            TitledNodeSkin titledNodeSkin = acquireTitledNodeSkin(watchPaneShifter);
            watchPaneShifter.setTitledNodeSkin(titledNodeSkin);
            var watchPane = titledNodeSkin.detachContent(); // 将WatchPane卸下来
            var shifter = shifterMap.get(watchPane.getClass());
            if (shifter == null) {
                shifter = new FlowPane();
                shifter.setPadding(new Insets(2d));
                shifter.setRowValignment(VPos.TOP);
                shifterMap.put(watchPane.getClass(), shifter);
                outerPanel.getChildren().add(shifter);
            }
            watchPaneShifter.setShifter(shifter);
            shifter.getChildren().add(watchPane);
        } else {
            log.error("An Unsupported type of InfoReporter!");
        }
    }

    private TitledNodeSkin acquireTitledNodeSkin(AbstractInfoReporter reporter) {
        long pId = reporter.getReportedProcessor().getProcessorId().getProcessorCode();
        var nodeSkin = MainPaneController.INSTANCE.getGraphEditor().getSkinLookup().lookupNode(pId);
        if (nodeSkin instanceof TitledNodeSkin titledNodeSkin) {
            return titledNodeSkin;
        } else {
            String err = "Unsupported skin!";
            log.error("Unsupported skin!");
            throw new RuntimeException(err);
        }
    }

    // 将WatchPane贴回去
    public void putBackItem(WatchPaneShifter watchPaneShifter) {
        TitledNodeSkin titledNodeSkin = watchPaneShifter.getTitledNodeSkin();
        var shifter = watchPaneShifter.getShifter();
        if (titledNodeSkin != null && shifter != null
                && shifter.getChildren().remove(titledNodeSkin.getContentRoot()))
            titledNodeSkin.attachContent(); // 将WatchPane贴回去
    }

    @Override
    public void removeItem(AbstractInfoReporter reporter) {
        if (reporter instanceof WatchPaneShifter watchPaneShifter) {
            putBackItem(watchPaneShifter);
            watchPaneShifter.setTitledNodeSkin(null);
            watchPaneShifter.setShifter(null);
        } else {
            log.error("An Unsupported type of InfoReporter!");
        }
    }

    @Override
    public void clearItems() {
        for (int i = 0; i < shifterList.size(); i++) {
            this.removeItem(shifterList.get(i));
        }
        for (var entry : shifterMap.entrySet()) {
            entry.getValue().getChildren().clear();
        }
        shifterMap.clear();
        outerPanel.getChildren().clear();
        shifterList.clear();
    }

    @Override
    public void refreshItems() {
        for (var watchPane : shifterList) {
            watchPane.getTitledNodeSkin().getContentRoot().refresh();
        }
    }
}
