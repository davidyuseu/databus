package sy.databus.view.monitor;


import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;


import sy.databus.organize.monitor.DevInfoReporter;
import static sy.databus.organize.monitor.MonitorGroup.IN_DEV;

@Log4j2
public class InDevTab extends BaseMonitorTab<DevInfoReporter> {

    @Getter
    private TableColumn<DevInfoReporter, String> itemNameColumn = new TableColumn<>("设备名称");
    @Getter
    private TableColumn<DevInfoReporter, String> inRateColumn = new TableColumn<>("输入速率");
    @Getter
    private TableColumn<DevInfoReporter, Integer> inPkgColumn = new TableColumn<>("输入帧数");
    @Getter
    private TableColumn<DevInfoReporter, String> devInfoColumn = new TableColumn<>("设备信息");

    public InDevTab() {
        super(IN_DEV);

        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        inRateColumn.setCellValueFactory(new PropertyValueFactory<>("inRateStr"));
        inPkgColumn.setCellValueFactory(new PropertyValueFactory<>("inPkg"));
        devInfoColumn.setCellValueFactory(new PropertyValueFactory<>("devInfo"));

        tableView.getColumns().addAll(itemNameColumn, inRateColumn, inPkgColumn, devInfoColumn);

    }
}
