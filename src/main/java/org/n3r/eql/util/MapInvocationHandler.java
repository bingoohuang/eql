package org.n3r.eql.util;

import com.google.common.collect.Maps;
import lombok.val;
import org.n3r.eql.base.EqlToProperties;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import static org.n3r.eql.util.P.toDbConvert;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/13.
 */
@SuppressWarnings("unchecked")
public class MapInvocationHandler implements InvocationHandler {
    private final Map<String, Object> context;
    private final Object bean;
    private Map<String, Object> merged;

    public MapInvocationHandler(Map<String, Object> context, Object bean) {
        this.context = context;
        this.bean = bean instanceof EqlToProperties
                ? ((EqlToProperties) bean).toProperties()
                : bean;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        val isGet = method.getName().equals("get");
        if (!isGet) { // 有其他方法调用时，退回到预先准备所有property的模式
            if (merged == null) {
                merged = mergeProperties(context, bean);
            }
            return method.invoke(merged, args);
        }

        Object arg0 = args[0];
        if (merged != null) { // 如果已经有退回的，则使用已退回模式
            return merged.get(arg0);
        }

        val propertyReader = new BeanPropertyReader(bean, (String) arg0);
        if (propertyReader.isPropertyExisted()) {
            return propertyReader.getPropertyValue();
        }

        return context != null ? context.get(arg0) : null;
    }

    public static Map<String, Object> proxy(Map<String, Object> context, Object bean) {
        val classLoader = P.class.getClassLoader();
        val handler = new MapInvocationHandler(context, bean);
        val proxy = Proxy.newProxyInstance(classLoader, new Class[]{Map.class}, handler);
        return (Map<String, Object>) proxy;
    }


    private static Map<String, Object> mergeProperties(
            Map<String, Object> context, Object bean) {
        Map<String, Object> map = Maps.newHashMap(context);
        if (bean == null) return map;

        if (bean instanceof Map) {
            map.putAll((Map<String, Object>) bean);
            return map;
        }

        if (bean instanceof EqlToProperties) {
            map.putAll(((EqlToProperties) bean).toProperties());
        } else {
            mergeBeanProperties(bean, map);
        }

        return map;
    }

    private static void mergeBeanProperties(Object bean, Map<String, Object> map) {
        mergeReadProperties(bean, map);
        mergeDeclaredFields(bean, map);
    }

    private static void mergeDeclaredFields(Object bean, Map<String, Object> map) {
        for (val field : bean.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(bean);
                value = toDbConvert(field, value);
                map.put(field.getName(), value);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private static void mergeReadProperties(
            Object bean, Map<String, Object> map) {
        val info = O.getBeanInfo(bean.getClass());

        for (val pDesc : info.getPropertyDescriptors()) {
            val method = pDesc.getReadMethod();
            if (method == null) continue;

            val value = O.invokeMethod(bean, method);
            Object propertyValue = toDbConvert(method, value.orNull());
            map.put(pDesc.getName(), propertyValue);
        }
    }

}
