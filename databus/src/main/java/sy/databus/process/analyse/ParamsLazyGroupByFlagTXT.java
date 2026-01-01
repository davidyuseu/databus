package sy.databus.process.analyse;

import io.netty.buffer.ByteBuf;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.entity.message.EfficientMessage;
import sy.databus.entity.property.RadioSelectObList;
import sy.databus.entity.signal.DATA_TASK_BEGIN;
import sy.databus.global.ProcessorType;
import sy.databus.global.WorkMode;
import sy.databus.process.Console;
import sy.databus.process.Processor;
import sy.databus.view.watch.MessageSeriesProcessorWatchPane;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.locks.ReentrantLock;


import static sy.databus.process.Console.Config.STATIC;

@Log4j2
@Processor(
        type = ProcessorType.PARAMS_RECORDER,
        pane = MessageSeriesProcessorWatchPane.class,
        coupledSubs = {ParamsUnion_CNTXT.class, ParamsCollector.class}
)
public class ParamsLazyGroupByFlagTXT extends CharNumParamsAnalyzerTXT {

    private static final Map<Integer, Map<Integer, Integer>> groupedFlags = new HashMap<>();

    private Map<Integer, Integer> flags = null;

    private static final ReentrantLock flagsLocker = new ReentrantLock();

    @Getter@Setter
    @Console(config = STATIC, display = "组号")
    private int groupId = 0;
    @Getter@Setter
    @Console(config = STATIC, display = "逻辑序号")
    private int logicId = 0;
    @Setter @Getter
    @Console(config = STATIC, display = "字节判别大小")
    private RadioSelectObList<String> flagSizeToSel;
    private int flagSize = 1;
    @Getter@Setter
    @Console(config = STATIC, display = "字节判别位置")
    private int bytePos = 0;


    @Override
    public void initialize() {
        super.initialize();

        if (flagSizeToSel == null) {
            flagSizeToSel = new RadioSelectObList<>(false, 0);
        }
        flagSizeToSel.getCandidateList().addAll("1字节"
                , "2字节"
                , "3字节"
                , "4字节");

        appendSlot(DATA_TASK_BEGIN.class, signal -> {
            if (flags != null)
                flags.clear();
            return true;
        });
    }

    @Override
    public void boot() throws Exception {
        super.boot();
        flags = groupedFlags
                .computeIfAbsent(groupId, k -> new HashMap<>());
        flagSize = flagSizeToSel.getSelIndex() + 1;
    }

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {
        super.reset(oriMode, targetMode);
        groupedFlags.clear();
        flagsLocker.lock();
        if (flags != null) {
            flags.clear();
            flags = null;
        }
        flagsLocker.unlock();
        flagSize = 1;
    }

    @Override
    protected void tailHandle(EfficientMessage message) {
        ByteBuf msg = message.getData();
        int mark = switch (flagSize) {
            case 1 -> msg.getByte(bytePos) & 0xff;
            case 2 -> msg.getShort(bytePos) & 0xffff;
            case 3 -> msg.getMedium(bytePos) & 0xffffff;
            case 4 -> msg.getInt(bytePos);
            default -> 0;
        };
        flagsLocker.lock();
        if (flags == null) {
            message.release();
            flagsLocker.unlock();
            return;
        }
        if (flags.get(logicId) != null) { // 已录入标识
            int flag = flags.get(logicId);
            flagsLocker.unlock();
            if (mark == flag) {
                super.tailHandle(message);
            } else {
                message.release();
            }
        } else {
            for (var m : flags.values()) {
                if (m == mark) {
                    flagsLocker.unlock();
                    message.release();
                    return;
                }
            }
            flags.put(logicId, mark);
            flagsLocker.unlock();
            super.tailHandle(message);
        }
    }

    @Override
    public void finish() {
        super.finish();
        Integer mark = null;
        flagsLocker.lock();
        if (flags != null)
            mark = flags.get(logicId);
        flagsLocker.unlock();
        if (crntFileToWrite != null && crntFileToWrite.exists() && mark != null) {
            String id ="机(" + String.format("%04X", mark) + ")";
            File newFile = new File(crntFileToWrite.getParent() + "\\"
                    + id
                    + name.get()
                    + "_"
                    + dateFormat2Mn4File.format(CachedClock.instance().currentTimeMillis()) + ".txt");
            if (!crntFileToWrite.renameTo(newFile)) {
                log.warn("Failed to rename '{}'", crntFileToWrite.getName());
            }
        }
        crntFileToWrite = null;
    }

}
