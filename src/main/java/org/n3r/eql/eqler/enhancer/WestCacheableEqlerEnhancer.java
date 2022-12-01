package org.n3r.eql.eqler.enhancer;

import com.github.bingoohuang.westcache.WestCacheFactory;
import com.github.bingoohuang.westcache.utils.Anns;
import com.google.auto.service.AutoService;
import lombok.val;
import org.n3r.eql.util.C;

@AutoService(EqlerEnhancer.class)
public class WestCacheableEqlerEnhancer implements EqlerEnhancer {

    @Override
    public boolean isEnabled(Class eqlerClass) {
        val className = "com.github.bingoohuang.westcache.WestCacheFactory";
        return C.classExists(className) && Anns.isFastWestCacheAnnotated(eqlerClass);
    }

    @Override
    public Object build(Class eqlerClass, Object implObject) {
        return WestCacheFactory.create(implObject);
    }
}
