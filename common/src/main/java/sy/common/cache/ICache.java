package sy.common.cache;

public interface ICache {
    // 存入缓存表前需要先清理，如释放一些引用防止内存泄漏，确保重新取出时是一个“干净”的对象
    void clean();

}
