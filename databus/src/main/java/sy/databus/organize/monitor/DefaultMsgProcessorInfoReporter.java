package sy.databus.organize.monitor;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.process.AbstractMessageProcessor;

import java.math.RoundingMode;
import java.text.NumberFormat;

public class DefaultMsgProcessorInfoReporter extends AbstractInfoReporter {

    private static final String INITIAL_RATE = "0 Kbps";
    private static final String RATE_UNIT = " Kbps";

    @Getter
    private int inPkg, outPkg;
    @Getter
    private String inRateStr = INITIAL_RATE, outRateStr = INITIAL_RATE; // 每秒
    @Getter
    private double inRate, outRate; // 每秒
    private long lastInBytes, lastOutBytes;

    private long lastTimestamp = 0L;

    private static NumberFormat numFormat = NumberFormat.getNumberInstance();
    static {
        // 默认小数位为1
        numFormat.setMaximumFractionDigits(1);
        // 需要四舍五入，（若不需要则使用RoudingMode.DOWN）
        numFormat.setRoundingMode(RoundingMode.UP);
    }

    public DefaultMsgProcessorInfoReporter(AbstractMessageProcessor processor) {
        super(processor, MonitorGroup.FRAME_PROC);
    }

    public DefaultMsgProcessorInfoReporter(AbstractMessageProcessor processor, MonitorGroup group) {
        super(processor, group);
    }

    private void updateInPkg() {
        inPkg = ((AbstractMessageProcessor) reportedProcessor).getInPkgCount();
    }

    private void updateOutPkg() {
        outPkg = ((AbstractMessageProcessor) reportedProcessor).getOutPkgCount();
    }
    // 每秒
    private void updateInRate(long crntTimestamp, long crntInBytes) {
        inRate = ((crntInBytes - lastInBytes) * 8) / (double)(crntTimestamp - lastTimestamp);
        inRateStr = numFormat.format(inRate) + RATE_UNIT;
    }
    // 每秒
    private void updateOutRate(long crntTimestamp, long crntOutBytes) {
        outRate = (crntOutBytes - lastOutBytes) * 8 / (double)(crntTimestamp - lastTimestamp);
        outRateStr = numFormat.format(outRate) + RATE_UNIT;
    }


    @Override
    public synchronized void updateInfo() {
        updateInPkg();
        updateOutPkg();

        long crntTimestamp = CachedClock.instance().currentTimeMillis();
        if (lastTimestamp == 0L) {
            lastTimestamp = crntTimestamp;
            return;
        }

        var msgProcessor = (AbstractMessageProcessor) reportedProcessor;
        long crntInBytes = msgProcessor.getInBytes();
        long crntOutBytes = msgProcessor.getOutBytes();
        updateInRate(crntTimestamp, crntInBytes);
        updateOutRate(crntTimestamp, crntOutBytes);

        lastInBytes = crntInBytes;
        lastOutBytes = crntOutBytes;

        lastTimestamp = crntTimestamp;
    }

    @Override
    public synchronized void resetInfo() {
        inPkg = outPkg = 0;
        inRate = outRate = 0d;
        inRateStr = outRateStr = INITIAL_RATE;

        lastTimestamp = 0L;

        lastInBytes = 0;
        lastOutBytes = 0;

        var msgProcessor = (AbstractMessageProcessor) reportedProcessor;
        msgProcessor.setInPkgCount(0);
        msgProcessor.setInBytes(0L);
        msgProcessor.setOutPkgCount(0);
        msgProcessor.setOutBytes(0L);
    }
}
