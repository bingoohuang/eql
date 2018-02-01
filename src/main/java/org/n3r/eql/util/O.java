package org.n3r.eql.util;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.spec.ParamsAppliable;
import org.n3r.eql.spec.Spec;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j @SuppressWarnings("unchecked")
public class O {
    public static <T> T populate(
            T object, Map<String, String> map,
            PropertyValueFilter... propertyValueFilters) {
        Map<String, String> params = new HashMap<String, String>(map);
        for (Method method : object.getClass().getMethods()) {
            if (!isSetterMethod(method)) continue;

            String propertyName = getPropertyNameFromSetter(method);
            if (!params.containsKey(propertyName)) continue;

            String propertyValue = params.get(propertyName);
            for (PropertyValueFilter pvf : propertyValueFilters) {
                propertyValue = pvf.filter(propertyValue);
            }

            boolean ok = populateProperty(object, method, propertyName, propertyValue);
            if (ok) params.remove(propertyName);
        }

//        for (Map.Entry<String, String> entry : params.entrySet()) {
//            log.warn("{}:{} is not recognized",
//                    entry.getKey(), entry.getValue());
//        }

        return object;
    }

    public static String getPropertyNameFromSetter(Method method) {
        String propertyName = method.getName().substring(3);
        char lower = Character.toLowerCase(propertyName.charAt(0));
        return lower + propertyName.substring(1);
    }

    public static boolean isSetterMethod(Method method) {
        Class<?> returnType = method.getReturnType();
        String methodName = method.getName();

        return Modifier.isPublic(method.getModifiers())
                && (returnType == Void.TYPE || returnType == void.class)
                && methodName.length() > 3
                && methodName.startsWith("set")
                && method.getParameterTypes().length == 1;
    }

    private static <T> boolean populateProperty(
            T object, Method method, String propertyName, String propertyValue) {
        Class<?> paramType = method.getParameterTypes()[0];
        try {
            if (paramType == String.class) {
                method.invoke(object, propertyValue);
                return true;
            } else if (paramType == Long.class || paramType == long.class) {
                if (propertyValue.matches("\\d+")) {
                    method.invoke(object, Long.parseLong(propertyValue));
                    return true;
                }
            } else if (paramType == Integer.class || paramType == int.class) {
                if (propertyValue.matches("\\d+")) {
                    method.invoke(object, Integer.parseInt(propertyValue));
                    return true;
                }
            } else if (paramType == Boolean.class || paramType == boolean.class) {
                method.invoke(object, Boolean.parseBoolean(propertyValue));
                return true;
            }
        } catch (Exception e) {
            log.warn("{}:{} is not used by {}", propertyName, propertyValue, e.getMessage());
        }

        return false;
    }

    @SneakyThrows
    public static <T> T createObject(Class<T> clazz, Spec spec) {
        Object object = Reflect.on(spec.getName()).create().get();

        if (!clazz.isInstance(object)) {
            throw new EqlExecuteException(spec.getName() + " does not implement " + clazz);
        }

        if (object instanceof ParamsAppliable)
            ((ParamsAppliable) object).applyParams(spec.getParams());

        return (T) object;
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
                || param instanceof Collection) {
            return ImmutableMap.of("_params", params);
        }

        return param;
    }

    public static boolean setProperty(
            Object object, PropertyDescriptor pd, Object value) {
        Method setter = pd.getWriteMethod();
        if (setter == null) return false;

        try {
            setAccessibleTrue(setter);
            setter.invoke(object, value);
            return true;
        } catch (Exception e) {
            log.warn("set value by error {}", e.getMessage());
        }

        return false;
    }

    @SneakyThrows
    public static Optional<Object> invokeMethod(Object bean, Method method) {
        try {
            return Optional.fromNullable(method.invoke(bean));
        } catch (InvocationTargetException e) {
            throw e.getCause() != null ? e.getCause() : e;
        }
    }

    @SneakyThrows
    public static BeanInfo getBeanInfo(Class<?> aClass) {
        return Introspector.getBeanInfo(aClass);
    }

    public interface ValueGettable {
        Object getValue();

        Object getValue(Class<?> returnType);
    }

    public static class ObjectGetter implements ValueGettable {
        private final Object value;

        public ObjectGetter(Object value) {
            this.value = value;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object getValue(Class<?> returnType) {
            return value;
        }
    }

    public static boolean setValue(
            Object mappedObject, String columnName, ValueGettable valueGettable) {
        if (mappedObject instanceof Map) {
            ((Map) mappedObject).put(columnName, valueGettable.getValue());
            return true;
        }

        int dotPos = columnName.indexOf('.');
        if (dotPos < 0)
            return setProperty(mappedObject, columnName, valueGettable);

        String property = columnName.substring(0, dotPos);
        Object propertyValue = getOrCreateProperty(property, mappedObject);
        if (propertyValue == null) return false;

        String nestProperty = columnName.substring(dotPos + 1);
        return setValue(propertyValue, nestProperty, valueGettable);
    }

    public static Object getOrCreateProperty(String propertyName, Object hostBean) {
        Object property = getProperty(propertyName, hostBean);
        if (property != null) return property;

        // There has to be a method get* matching this segment
        Class<?> returnType = getPropertyType(propertyName, hostBean);

        if (Map.class.isAssignableFrom(returnType))
            property = Maps.newHashMap();

        if (property == null) property = Reflect.on(returnType).create().get();

        setProperty(hostBean, propertyName, new ObjectGetter(property));

        return property;
    }

    public static Class<?> getPropertyType(String propertyName, Object hostBean) {
        String methodName = getGetMethodName(propertyName);
        try {
            Method m = getAccessibleMethod(hostBean, methodName);
            return m.getReturnType();
        } catch (NoSuchMethodException e) {
            log.debug("NoSuchMethodException invoke get method of property {} of {}",
                    propertyName, hostBean);
            // ignore
        } catch (Exception e) {
            log.debug("invoke method exception", e);
            // ignore
        }

        try {
            Field field = getAccessibleField(hostBean, propertyName);
            return field.getType();
        } catch (Exception e) {
            log.debug("invoke method exception", e);
        }

        throw new RuntimeException("unable to get property "
                + propertyName + " type of " + hostBean);
    }


    public static Object getProperty(String propertyName, Object hostBean) {
        String methodName = getGetMethodName(propertyName);
        try {
            Method m = getAccessibleMethod(hostBean, methodName);
            return m.invoke(hostBean);
        } catch (NoSuchMethodException e) {
            log.debug("NoSuchMethodException invoke get method " +
                    "of property {} of {}", propertyName, hostBean);
            // ignore
        } catch (Exception e) {
            log.debug("invoke method exception", e);
            // ignore
        }

        try {
            Field field = getAccessibleField(hostBean, propertyName);
            return field.get(hostBean);
        } catch (Exception e) {
            log.debug("invoke method exception", e);
        }

        throw new RuntimeException("unable to get property value "
                + propertyName + " of bean " + hostBean);
    }

    private static boolean setProperty(
            Object hostBean, String propertyName, ValueGettable valueGettable) {
        if (hostBean instanceof Map) {
            ((Map) hostBean).put(propertyName, valueGettable.getValue());
            return true;
        }

        return setBeanProperty(hostBean, propertyName, valueGettable);
    }

    private static boolean setBeanProperty(
            Object hostBean, String propertyName, ValueGettable valueGettable) {
        String methodName = getSetMethodName(propertyName);
        try {
            Method m = getAccessibleMethod(hostBean, methodName);
            m.invoke(hostBean, valueGettable.getValue(m.getReturnType()));
            return true;
        } catch (NoSuchMethodException e) {
            // ignore
        } catch (Exception e) {
            log.debug("invoke set method exception", e);
        }

        try {
            Field field = getAccessibleField(hostBean, propertyName);
            field.set(hostBean, valueGettable.getValue(field.getType()));
            return true;
        } catch (NoSuchFieldException e) {
            // ignore
        } catch (Exception e) {
            log.debug("invoke set field exception", e);
        }

        return false;
    }

    public static String getGetMethodName(String propertyName) {
        char upper = Character.toTitleCase(propertyName.charAt(0));
        return "get" + upper + propertyName.substring(1);
    }

    public static String getSetMethodName(String propertyName) {
        char upper = Character.toTitleCase(propertyName.charAt(0));
        return "set" + upper + propertyName.substring(1);
    }

    public static void setAccessibleTrue(AccessibleObject m) {
        if (!m.isAccessible()) m.setAccessible(true);
    }

    public static Method getAccessibleMethod(Object hostBean, String methodName)
            throws NoSuchMethodException {
        Method m = hostBean.getClass().getMethod(methodName);
        setAccessibleTrue(m);
        return m;
    }

    public static Field getAccessibleField(Object hostBean, String propertyName)
            throws NoSuchFieldException {
        Field field = hostBean.getClass().getDeclaredField(propertyName);
        setAccessibleTrue(field);
        return field;
    }
}
