package org.n3r.eql.util;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

public class Ob {
    public static Object createInstance(Class<?> type) {
        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator instantiator = objenesis.getInstantiatorOf(type);
        return instantiator.newInstance();
    }

}
