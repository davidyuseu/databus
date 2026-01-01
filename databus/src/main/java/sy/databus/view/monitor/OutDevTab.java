package sy.databus.view.monitor;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Getter;
import sy.databus.organize.monitor.DevInfoReporter;

import static sy.databus.organize.monitor.MonitorGroup.OUT_DEV;


public class OutDevTab extends BaseMonitorTab<DevInfoReporter> {

    @Getter
    private TableColumn<DevInfoReporter, String> itemNameColumn = new TableColumn<>("设备名称");
    @Getter
    private TableColumn<DevInfoReporter, String> outRateColumn = new TableColumn<>("输出速率");
    @Getter
    private TableColumn<DevInfoReporter, Integer> outPkgColumn = new TableColumn<>("输出帧数");
    @Getter
    private TableColumn<DevInfoReporter, String> devInfoColumn = new TableColumn<>("设备信息");

    public OutDevTab() {
        super(OUT_DEV);

        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        outRateColumn.setCellValueFactory(new PropertyValueFactory<>("outRateStr"));
        outPkgColumn.setCellValueFactory(new PropertyValueFactory<>("outPkg"));
        devInfoColumn.setCellValueFactory(new PropertyValueFactory<>("devInfo"));



        tableView.getColumns().addAll(itemNameColumn, outRateColumn, outPkgColumn, devInfoColumn);
    }
}
