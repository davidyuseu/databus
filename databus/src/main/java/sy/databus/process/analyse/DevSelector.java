package sy.databus.process.analyse;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import sy.databus.global.ProcessorType;
import sy.databus.process.Console;
import sy.databus.process.Processor;
import sy.databus.process.frame.MessageSeriesProcessor;
import sy.databus.view.watch.MessageSeriesProcessorWatchPane;

import static sy.databus.process.Console.Config.STATIC;

@Processor(
        type = ProcessorType.FRAME,
        pane = MessageSeriesProcessorWatchPane.class
)
public class DevSelector extends MessageSeriesProcessor<ByteBuf> {

    @Setter @Getter
    @Console(config = STATIC, display = "设备类型")
    private int devType = 0;

    @Setter @Getter
    @Console(config = STATIC, display = "设备号")
    private int devNum = 0;
}
