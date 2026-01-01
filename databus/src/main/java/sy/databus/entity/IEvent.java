package sy.databus.entity;

/** marker interface */
public interface IEvent {

    /**
     * @param sb 追加Dump信息到字符串中
     * */
    void appendDumpInfo(StringBuilder sb);
}
