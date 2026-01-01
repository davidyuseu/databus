package sy.databus.process.frame.handler.filter;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.databus.entity.message.IMessage;
import sy.databus.process.Console;
import sy.databus.process.ProcessorInitException;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.frame.handler.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.frame.handler.Handler.Category.HANDLER_FRAME;

@Handler(
        category = HANDLER_FRAME,
        group = "filter"
)
@Log4j2
public class MultiByteFlagFilter extends AbstractTransHandler<IMessage<ByteBuf>> {

    @Setter @Getter
    @Console(config = STATIC, display = "多标识")
    private String strflags = "";

    @Setter @Getter
    @Console(config = STATIC, display = "标识位置")
    private String strPoss = "";

    private Map<Integer, List<Integer>> posFlags = new HashMap<>();

    public MultiByteFlagFilter() {}

    @Override
    public void initialize() {

    }

    @Override
    public void handle(IMessage<ByteBuf> msg) throws Exception {
        ByteBuf buf = msg.getData();
        boolean pass;
        for (var entry : posFlags.entrySet()) {
            int pos = entry.getKey();
            if (pos >= buf.capacity()) {
                log.error("标识位置越界！");
                msg.release();
                return;
            }
            List<Integer> flags = entry.getValue();
            pass = false;
            for (int flag : flags) {
                if (buf.getUnsignedByte(pos) == flag) {
                    pass = true;
                    break;
                }
            }
            if (!pass) {
                msg.release();
                return;
            }
        }
        fireNext(msg);
    }

    @Override
    public void boot() {
        super.boot();
        posFlags.clear();
        if (!strflags.isEmpty() && !strPoss.isEmpty()) {
            // 字节位置
            String[] possArr = strPoss.split(";");
            // 各标识
            String[] flagsArr = strflags.split(";");
            if (possArr.length != flagsArr.length) {
                String errMsg = "多标识判别handler中字节位置数与标识组数不匹配！";
                log.error(errMsg);
                throw new ProcessorInitException(errMsg);
            }
            for (int i = 0; i < possArr.length; i++) {
                String[] flagGroup = flagsArr[i].split("\\|");
                List<Integer> flagList = new ArrayList<>();
                for (String flag : flagGroup) {
                    flagList.add(Integer.parseInt(flag.trim(), 16));
                }
                posFlags.put(Integer.parseInt(possArr[i].trim()), flagList);
            }
        }
    }
}
