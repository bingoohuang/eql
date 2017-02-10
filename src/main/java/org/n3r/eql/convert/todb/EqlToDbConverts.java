package org.n3r.eql.convert.todb;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import lombok.val;
import org.n3r.eql.convert.EqlConvertAnn;
import org.n3r.eql.convert.EqlConverts;

import java.lang.reflect.AccessibleObject;
import java.util.List;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/10.
 */
public class EqlToDbConverts {
    static LoadingCache<AccessibleObject, Optional<EqlerToDbConverter>> toDbConverterCache =
            CacheBuilder.newBuilder().build(new CacheLoader<AccessibleObject, Optional<EqlerToDbConverter>>() {
                @Override
                public Optional<EqlerToDbConverter> load(AccessibleObject accessibleObject) throws Exception {
                    List<EqlConvertAnn<ToDbConvert>> ecas = Lists.newArrayList();
                    EqlConverts.searchEqlConvertAnns(accessibleObject, ecas, ToDbConvert.class);
                    if (ecas.isEmpty()) return Optional.absent();

                    val eqlerToDbConverter = new EqlerToDbConverter();
                    for(val eca : ecas) {
                        eqlerToDbConverter.addConvertAnn(eca);
                    }

                    return Optional.of(eqlerToDbConverter);
                }
            });

    public static Optional<EqlerToDbConverter> getConverter(AccessibleObject accessibleObject) {
        return toDbConverterCache.getUnchecked(accessibleObject);
    }
}
