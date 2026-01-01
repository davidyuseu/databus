package sy.databus.process.frame.handler.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.netty.buffer.ByteBuf;
import javafx.scene.Node;
import lombok.Getter;
import lombok.Setter;
import sy.common.tmresolve.ResultStruct;
import sy.databus.entity.message.IMessage;
import sy.databus.entity.property.ConsecutiveBufSafeProperty;
import sy.databus.entity.property.RadioSelectObList;
import sy.databus.entity.property.RadioSelectObListDeserializer;
import sy.databus.entity.property.SFile;
import sy.databus.organize.ComponentCustomization;
import sy.databus.process.Console;
import sy.databus.process.frame.AbstractTransHandler;

import java.util.Set;

import static sy.databus.process.Console.Config.*;

/*@Handler(
        category = HANDLER_FRAME,
        name = "测试处理器"
)*/
public class TestHandler extends AbstractTransHandler<IMessage<ByteBuf>> implements ComponentCustomization {

    @Override
    public void customise(Set<Node> controllers) {
        System.out.println("customising!");
    }

    @Override
    public void uncustomize(Set<Node> controllers) {
        System.out.println("uncustomising!");
    }

    @JsonDeserialize(using = RadioSelectObListDeserializer.class)
    public static class RadioSelObList_ResultStruct extends RadioSelectObList<ResultStruct> {
        // 必须有无参构造器，否则无法反序列化
        public RadioSelObList_ResultStruct(){}

        public RadioSelObList_ResultStruct(boolean saveList, int selIndex, ResultStruct... candidateItems) {
            super(saveList, selIndex, candidateItems);
        }

        @Override
        public String getItemString(ResultStruct resultStruct) {
            return resultStruct.getItemName();
        }
    }

    @Getter @Setter
    @Console(report = Console.Report.RUNTIME , display = "飞机号")
    private int planeNum = 3;

    @Getter @Setter
    @Console(config = STATIC, display = "加载配置文件")
    private SFile confTest = SFile.buildDefaultFile("*");

    @Setter @Getter
    @Console(config = NON_RUNNING, display = "机号位置")
    int planeIdPos = 0;

    @Getter @Setter
    @Console(config = DYNAMIC, display = "帧标识")
    private ConsecutiveBufSafeProperty bufSafeProperty = new ConsecutiveBufSafeProperty("eb900401");

    @Getter @Setter
    @Console(config = NON_RUNNING, display = "测试项")
    private RadioSelObList_ResultStruct radioSelectObList = new RadioSelObList_ResultStruct(true,
            1,
            new ResultStruct(0, "test1"),
            new ResultStruct(1, "test2"),
            new ResultStruct(2, "test3"),
            new ResultStruct(3, "test4")
    );

    @Getter @Setter
    @Console(config = STATIC, display = "测试项")
    private RadioSelectObList<String> radioSelectObList1 = new RadioSelectObList<>( true,
            -1,
            "item1", "item2", "item3", "item4", "item5"
    );
/*  //不支持匿名内部类
    @JsonDeserialize(using = RadioSelectObListDeserializer.class)
    private RadioSelectObList<ResultStruct> radioSelectObList = new RadioSelectObList<>(true,
                        0,
                        new ResultStruct(0, "test1"),
                        new ResultStruct(1, "test2"),
                        new ResultStruct(2, "test3")){

        @Override
        public String getItemString(ResultStruct resultStruct) {
            return resultStruct.getItemName();
        }

    };
 */
//    private RadioSelectObList<String> radioSelectObList = new RadioSelectObList<String>(true, 0, "test1", "test2");


    @Override
    public void initialize() {
        // #-
//        radioSelectObList.setOccupiedIndexGroup(LSNReplayFileReader.selectedDevs);
    }

    @Override
    public void handle(IMessage<ByteBuf> msg) throws Exception {
        fireNext(msg);
    }
}
