package org.n3r.eql.util;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class O {
    static Logger log = LoggerFactory.getLogger(O.class);

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

    public static boolean setProperty(Object object, PropertyDescriptor pd, Object value) {
        Method setter = pd.getWriteMethod();
        if (setter == null) return false;

        try {
            if (!setter.isAccessible()) setter.setAccessible(true);
            setter.invoke(object, value);
            return true;
        } catch (Exception e) {
            log.warn("set value by error {}", e.getMessage());
        }

        return false;
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

    public static void setValue(Object mappedObject, String columnName, Object value) {
        int dotPos = columnName.indexOf('.');
        if (dotPos < 0) {
            setProperty(mappedObject, columnName, value);
            return;
        }

        String property = columnName.substring(0, dotPos);
        Object propertyValue = createProperty(property, mappedObject);
        if (propertyValue == null) return;

        setValue(propertyValue, columnName.substring(dotPos + 1), value);
    }

    public static Object createProperty(String propertyName, Object hostBean) {
        // There has to be a method get* matching this segment
        Class<?> returnType = getPropertyType(propertyName, hostBean);

        Object o = Reflect.on(returnType).create().get();
        setProperty(hostBean, propertyName, o);
        return o;
    }

    public static Class<?> getPropertyType(String propertyName, Object hostBean) {
        Class<?> returnType = null;
        try {
            String methodName = "get" + Character.toTitleCase(propertyName.charAt(0)) + propertyName.substring(1);
            Method m = hostBean.getClass().getMethod(methodName);
            returnType = m.getReturnType();
        } catch (NoSuchMethodException e) {

        }

        if (returnType == null) {
            try {
                Field field = hostBean.getClass().getDeclaredField(propertyName);
                returnType = field.getType();
            } catch (Exception e) {
            }

        }
        return returnType;
    }

    private static boolean setProperty(Object hostBean, String propertyName, Object propertyValue) {
        String methodName = "set" + Character.toTitleCase(propertyName.charAt(0)) + propertyName.substring(1);
        try {
            Method m = hostBean.getClass().getMethod(methodName);
            if (!m.isAccessible()) m.setAccessible(true);
            m.invoke(hostBean, propertyValue);
            return true;
        } catch (Exception e) {
            //
        }

        try {
            Field field = hostBean.getClass().getDeclaredField(propertyName);
            if (field != null) {
                if (!field.isAccessible()) field.setAccessible(true);
                field.set(hostBean, propertyValue);
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }
}
