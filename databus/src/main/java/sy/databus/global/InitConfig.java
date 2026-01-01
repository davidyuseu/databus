package sy.databus.global;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import sy.common.util.SJsonUtil;
import sy.databus.process.AbstractIntegratedProcessor;

import java.io.File;

public class InitConfig {
    @JsonIgnore
    private static final String INIT_PATH = ".\\init\\initConfig.json";

    @JsonIgnore
    private static InitConfig INSTANCE;

    public InitConfig(){
        INSTANCE = this;
    };


    @Setter @Getter
    private String plane1Addr;
    @Setter @Getter
    private String plane2Addr;

    @Setter @Getter
    private double windowWeight;
    @Setter @Getter
    private double windowHeight;
    @Setter @Getter
    private boolean outputSysLog;
    @Setter @Getter
    private int editModeKey;

    @Setter @Getter
    private AbstractIntegratedProcessor singleFileReader; // 全局回放文件读取器

    public static InitConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = SJsonUtil.jsonFileToBean(new File(INIT_PATH), InitConfig.class);
        }
        return INSTANCE;
    }

    public static void saveConfig() throws Exception {
        if (INSTANCE == null) {
            throw new Exception("The instance of " + InitConfig.class.getSimpleName() + "is null!");
        }
        SJsonUtil.objToJsonFile(INSTANCE, new File(INIT_PATH));
    }
}
