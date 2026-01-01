package sy.databus.entity.message.metadata;

/**
 * 单个元参数
 * */
public interface IMetaParam {

    /** 根据参数特性的不同，有些实现浅复制（如ProcessorId），有些实现深复制（如消息号）*/
    IMetaParam clone();

}
