package sy.databus.entity.message.metadata;

/**
 * 元数据
 * ps:
 * 存入元参数中的数据以及其它元数据都只能是堆内存数据，元数据实现了{@link #clone()}操作，没有retain和release方法，
 * 所以存入堆外数据会导致内存泄漏。
 * */
public interface IMetadata {
    /** 拷贝元数据*/
    IMetadata clone();

    /** 报文诞生的时间戳*/
    // 获取生成时间戳
    long getBirthTimestamp();
    // 设置生成时间戳
    void setBirthTimestamp(long birthTime);

    /** 元参数映射表*/
    // 插入一个元参数
    void putParam(IMetaParam param);
    // 拿出一个元参数
    IMetaParam takeParam(Class<? extends IMetaParam> paramClazz);
    // 得到一个元参数
    IMetaParam getParam(Class<? extends IMetaParam> paramClazz);

}
