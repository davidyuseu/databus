package sy.databus.view.monitor;

import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.databus.MainPaneController;
import sy.databus.RightPaneController;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.organize.monitor.MonitorGroup;


public abstract class BaseMonitorTab<T extends AbstractInfoReporter> extends Tab implements MonitorTab<T>{

    @Getter
    protected TableView<T> tableView = new TableView<>();
    @Getter
    protected SSyncObservableList<T> sItems = new SSyncObservableList<>();


    @Override
    public void addItem(T reporter) {
        sItems.add(reporter);
    }

    @Override
    public void removeItem(T reporter) {
        sItems.remove(reporter);
    }

    @Override
    public void clearItems() {
        sItems.clear();
    }

    @Override
    public void refreshItems() {
        tableView.refresh();
    }

    public BaseMonitorTab(MonitorGroup group) {
        super(group.getDisplay());
        this.setContent(tableView);
        this.setClosable(false);

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_LAST_COLUMN);
        tableView.setItems(sItems);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                RightPaneController.adaptControllers(newSelection.getReportedProcessor());
            }
        });

        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (mouseEvent.getClickCount() == 2
                    && mouseEvent.getButton() == MouseButton.SECONDARY) {
                var reporter = tableView.getSelectionModel().getSelectedItem();
                if (reporter != null)
                    MainPaneController.reloadOriDataView(reporter.getReportedProcessor());
            }
        });

    }
}
