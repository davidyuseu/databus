package sy.databus.view.watch;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import lombok.extern.log4j.Log4j2;
import sy.common.fx.ui.SGeneralGridPane;
import sy.databus.organize.monitor.DefaultMsgProcessorInfoReporter;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.grapheditor.api.GNodeSkin;

import static sy.databus.view.customskins.TitledSkinController.DEFAULT_TITLED_NODE_HEIGHT;

@WatchPaneConfig(initialHeight = DEFAULT_TITLED_NODE_HEIGHT)
@Log4j2
public class OutputProcessorWatchPane extends FoldingWatchPane {

    private SGeneralGridPane statisticPane = new SGeneralGridPane();
    Label placeHolder = new Label(" ");
    Label vRate = new Label("速率");
    Label vPkg = new Label("包数");

    Label vOutput = new Label("输出");
    Label vOutRate = new Label();
    Label vOutPkg = new Label();

    private Circle outputLight = new Circle(4, 4, 8, Color.GRAY);

    public OutputProcessorWatchPane() {
        headerBox.setRight(outputLight);
        BorderPane.setAlignment(outputLight, Pos.CENTER);

        vOutput.setPrefWidth(86);
        vOutput.setAlignment(Pos.CENTER);

        // 头    输入  输出
        statisticPane.addRow(0, placeHolder, vOutput);
        // 速率
        statisticPane.addRow(1, vRate, vOutRate);
        // 包数：
        statisticPane.addRow(2, vPkg, vOutPkg);
        statisticPane.setGrowInner();
        statisticPane.setInternalGridLineVisible(true);
        statisticPane.setInnerPadding(3);

        this.addToContent(statisticPane);
    }

    @Override
    public void embeddedInRootNodeSkin(GNodeSkin rootNodeSkin) {
        super.embeddedInRootNodeSkin(rootNodeSkin);
        /** 必须在embeddedInRootNodeSkin方法中，待嵌入rootSkin后才可操作rootSkin，确保其不为null*/
        // 设置FrameProcessorPane的初始高度
//        rootSkin.getItem().setHeight(DEFAULT_PANE_HEIGHT); // *不能这样设置，否则回退时由于高度改变了，会出现灰色背景
    }

    @Override
    public void associateWith(AbstractIntegratedProcessor processor) {
        super.associateWith(processor);
    }

    @Override
    public void refresh() {
        var reporter = this.integratedProcessor.getInfoReporter();
        if (reporter instanceof DefaultMsgProcessorInfoReporter msgProcReporter) {
            msgProcReporter.updateInfo();
            if (expandedProperty.get()) {
                vOutPkg.setText(String.valueOf(msgProcReporter.getOutPkg()));
                vOutRate.setText(msgProcReporter.getOutRateStr());
            }
            outputLight.setFill(msgProcReporter.getOutRate() > 0 ? Color.GREEN : Color.GREY);
        } else {
            log.error("This watchPane need a {} to acquire some infos!",
                    DefaultMsgProcessorInfoReporter.class.getSimpleName());
        }
    }

    @Override
    public void stopForEditMode() {
        outputLight.setFill(Color.GREY);

        vOutPkg.setText("0");
        vOutRate.setText("0 Kbps");
    }
}
