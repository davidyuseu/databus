package excel.cache.selector;

import excel.cache.ReadCache;
import org.apache.poi.openxml4j.opc.PackagePart;

/**
 * Select the cache
 *
 * @author Jiaju Zhuang
 **/
public interface ReadCacheSelector {

    /**
     * Select a cache
     *
     * @param sharedStringsTablePackagePart
     * @return
     */
    ReadCache readCache(PackagePart sharedStringsTablePackagePart);
}
