package sy.databus.view.monitor;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Getter;

import lombok.extern.log4j.Log4j2;
import sy.databus.organize.monitor.DefaultMsgProcessorInfoReporter;

import static sy.databus.organize.monitor.MonitorGroup.FRAME_PROC;

@Log4j2
public class FrameProcTab extends BaseMonitorTab<DefaultMsgProcessorInfoReporter> {

    @Getter
    private TableColumn<DefaultMsgProcessorInfoReporter, String> itemNameColumn = new TableColumn<>("处理名称");
    @Getter
    private TableColumn<DefaultMsgProcessorInfoReporter, String> inRateColumn = new TableColumn<>("输入速率");
    @Getter
    private TableColumn<DefaultMsgProcessorInfoReporter, Integer> inPkgColumn = new TableColumn<>("输入帧数");
    @Getter
    private TableColumn<DefaultMsgProcessorInfoReporter, String> outRateColumn = new TableColumn<>("输出速率");
    @Getter
    private TableColumn<DefaultMsgProcessorInfoReporter, Integer> outPkgColumn = new TableColumn<>("输出帧数");

    public FrameProcTab() {
        super(FRAME_PROC);

        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        inRateColumn.setCellValueFactory(new PropertyValueFactory<>("inRateStr"));
        inPkgColumn.setCellValueFactory(new PropertyValueFactory<>("inPkg"));
        outRateColumn.setCellValueFactory(new PropertyValueFactory<>("outRateStr"));
        outPkgColumn.setCellValueFactory(new PropertyValueFactory<>("outPkg"));
        tableView.getColumns().addAll(itemNameColumn, inRateColumn, inPkgColumn, outRateColumn, outPkgColumn);

    }

}
