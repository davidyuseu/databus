package sy.databus.entity.message.metadata.params;

import sy.databus.entity.message.metadata.IMetaParam;

public record SimplePlaneId(int planeId) implements IMetaParam  {
    @Override
    public IMetaParam clone() {
        return this;
    }
}
