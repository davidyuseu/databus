package sy.common.cache;

import lombok.SneakyThrows;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存ICache对象，所有实现ICache接口的子类必须具有无参构造方法
 * */
public class CacheFactory {

    private static Map<Class<? extends ICache>, LinkedList<WeakReference<ICache>>> weakRefInstances
            = new ConcurrentHashMap<>();

    /**
     * 创建可回收的弱引用,当没有对象引用时，缓存对象将被gc掉
     * */
    @SneakyThrows
    public synchronized static ICache getWeakCache(Class<? extends ICache> clazz) {
        LinkedList<WeakReference<ICache>> refList = weakRefInstances.get(clazz);
        if (refList != null) {
            for (int i = 0; i < refList.size(); i++) {
                ICache cache = refList.get(i).get();
                if (cache != null) {
                    refList.remove(i);
                    return cache;
                } else {
                    refList.remove(i);
                }
            }
        }
        return null;
    }

    public synchronized static void recycle(ICache toCache) {
        /** should call in FX thread*/
        toCache.clean();
        LinkedList<WeakReference<ICache>> refList
                = weakRefInstances.computeIfAbsent(toCache.getClass(), k -> new LinkedList<>());
        refList.add(new WeakReference<>(toCache));
    }

}
