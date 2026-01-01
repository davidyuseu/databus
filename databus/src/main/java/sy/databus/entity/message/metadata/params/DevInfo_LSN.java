package sy.databus.entity.message.metadata.params;

import sy.databus.entity.message.metadata.IMetaParam;

public record DevInfo_LSN(byte devNum) implements IMetaParam {

    @Override
    public IMetaParam clone() {
        return this;
    }
}
