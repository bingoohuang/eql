package org.n3r.eql.util;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import org.n3r.eql.ex.EqlExecuteException;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.List;

public class O {
    public static Object createInstanceByObjenesis(Class<?> type) {
        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator instantiator = objenesis.getInstantiatorOf(type);
        return instantiator.newInstance();
    }


    public static <T> boolean in(T target, T... compares) {
        for (T compare : compares)
            if (Objects.equal(target, compare)) return true;

        return false;
    }


    public static Object createSingleBean(Object[] params) {
        if (params == null || params.length == 0) return new Object();

        if (params.length > 1) return ImmutableMap.of("_params", params);

        // 只剩下length == 1的情况
        Object param = params[0];
        if (param == null
                || param.getClass().isPrimitive()
                || Primitives.isWrapperType(param.getClass())
                || param instanceof String
                || param.getClass().isArray()
                || param instanceof List) {
            return ImmutableMap.of("_params", params);
        }

        return param;
    }

    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    public static boolean isNotEmpty(Object obj) {
        return isNotNull(obj) ? !obj.toString().equals("") : false;
    }


    public static Optional<Object> invokeMethod(Object bean, Method method) {
        try {
            return Optional.fromNullable(method.invoke(bean));
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    public static BeanInfo getBeanInfo(Class<?> aClass) {
        try {
            return Introspector.getBeanInfo(aClass);
        } catch (IntrospectionException e) {
            throw new EqlExecuteException(e);
        }
    }

}
