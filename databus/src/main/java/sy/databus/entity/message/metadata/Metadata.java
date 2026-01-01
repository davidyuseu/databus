package sy.databus.entity.message.metadata;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class Metadata implements IMetadata {
    /** 报文诞生的时间戳*/
    private long birthTimestamp;
        public long getBirthTimestamp(){ return birthTimestamp; }
        public void setBirthTimestamp(long birthTimestamp){ this.birthTimestamp = birthTimestamp; }

    /** 元参数映射表*/
    @Getter
    Map<Class<? extends IMetaParam>, IMetaParam> metaParams = new HashMap<>();
        @Override
        public void putParam(IMetaParam param) {
            metaParams.put(param.getClass(), param);
        }
        @Override
        public IMetaParam takeParam(Class<? extends IMetaParam> paramClazz) {
            return metaParams.remove(paramClazz);
        }
        @Override
        public IMetaParam getParam(Class<? extends IMetaParam> paramClazz) {
            return metaParams.get(paramClazz);
        }

    /** 元数据的构造方法与其{@link #clone()}方法息息相关*/
    public Metadata(long birthTimestamp) {
        this.birthTimestamp = birthTimestamp;
    }

    @Override
    public IMetadata clone() {
        Metadata newMetadata = new Metadata(this.birthTimestamp);
        Map newMetaParams = newMetadata.getMetaParams();
        for (Map.Entry<Class<? extends IMetaParam>, IMetaParam> entry : metaParams.entrySet()) {
            newMetaParams.put(entry.getKey(), entry.getValue().clone());
        }
        return newMetadata;
    }

}
