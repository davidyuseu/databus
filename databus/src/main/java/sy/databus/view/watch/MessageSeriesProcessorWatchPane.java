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
import sy.databus.process.AbstractMessageProcessor;
import sy.databus.process.ProcessorInitException;
import sy.grapheditor.api.GNodeSkin;

import static sy.databus.view.customskins.TitledSkinController.DEFAULT_TITLED_NODE_HEIGHT;

@WatchPaneConfig(initialHeight = DEFAULT_TITLED_NODE_HEIGHT)
@Log4j2
public class MessageSeriesProcessorWatchPane extends FoldingWatchPane {

    private SGeneralGridPane statisticPane = new SGeneralGridPane();
    Label placeHolder = new Label(" ");
    Label vRate = new Label("速率");
    Label vPkg = new Label("包数");
    //    Label vBytes = new Label("字节数");

    Label vInput = new Label("输入");
    Label vInRate = new Label();
    Label vInPkg = new Label();
    //    Label vInBytes = new Label();

    Label vOutput = new Label("输出");
    Label vOutRate = new Label();
    Label vOutPkg = new Label();
    //    Label vOutBytes = new Label();

    private Circle inputLight = new Circle(4, 4, 8, Color.GRAY);
    private Circle outputLight = new Circle(4, 4, 8, Color.GRAY);

    public MessageSeriesProcessorWatchPane() {
        headerBox.setLeft(inputLight);
        headerBox.setRight(outputLight);
        BorderPane.setAlignment(inputLight, Pos.CENTER);
        BorderPane.setAlignment(outputLight,Pos.CENTER);

        vInput.setPrefWidth(86);
        vOutput.setPrefWidth(86);
        vInput.setAlignment(Pos.CENTER);
        vOutput.setAlignment(Pos.CENTER);

        // 头    输入  输出
        statisticPane.addRow(0, placeHolder, vInput, vOutput);
        // 速率
        statisticPane.addRow(1, vRate, vInRate, vOutRate);
        // 包数：
        statisticPane.addRow(2, vPkg, vInPkg, vOutPkg);
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
        checkProcessorType();
    }

    protected void checkProcessorType() {
        if (!(integratedProcessor instanceof AbstractMessageProcessor))
            throw new ProcessorInitException("'" + MessageSeriesProcessorWatchPane.class.getSimpleName()
                    + "' must adapt '"
                    + AbstractMessageProcessor.class.getSimpleName() + "'!");
    }


    @Override
    public void refresh() {
        var reporter = this.integratedProcessor.getInfoReporter();
        if (reporter instanceof DefaultMsgProcessorInfoReporter msgProcReporter) {
            msgProcReporter.updateInfo();
            if (expandedProperty.get()) {
                vInPkg.setText(String.valueOf(msgProcReporter.getInPkg()));
                vInRate.setText(msgProcReporter.getInRateStr());
                vOutPkg.setText(String.valueOf(msgProcReporter.getOutPkg()));
                vOutRate.setText(msgProcReporter.getOutRateStr());
            }
            inputLight.setFill(msgProcReporter.getInRate() > 0 ? Color.GREEN : Color.GREY);
            outputLight.setFill(msgProcReporter.getOutRate() > 0 ? Color.GREEN : Color.GREY);
        } else {
            log.error("This watchPane need a {} to acquire some infos!",
                    DefaultMsgProcessorInfoReporter.class.getSimpleName());
        }
    }


    @Override
    public void stopForEditMode() {
        inputLight.setFill(Color.GREY);
        outputLight.setFill(Color.GREY);

        vInPkg.setText("0");
        vInRate.setText("0 Kbps");

        vOutPkg.setText("0");
        vOutRate.setText("0 Kbps");

    }

}
