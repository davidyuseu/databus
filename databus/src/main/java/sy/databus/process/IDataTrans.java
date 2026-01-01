package sy.databus.process;

public interface IDataTrans<T> {
    /**
     * @Description: handler需实现的数据转发接口
     * @param t 向下级转发的数据
     * @return void
     */
    void fireNext(T t) throws Exception;

    IEventProc<T> getNextHandler();

    void setNextHandler(IEventProc<T> handler);
}
