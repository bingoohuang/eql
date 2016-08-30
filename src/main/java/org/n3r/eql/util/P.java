package org.n3r.eql.util;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.n3r.eql.base.EqlToProperties;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("unchecked")
public class P {

    public static Map<String, Object> mergeProperties(
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
            mergeDeclaredProperties(bean, map);
            mergeReadProperties(bean, map);
        }

        return map;
    }

    private static void mergeDeclaredProperties(
            Object bean, Map<String, Object> map) {
        for (Field field : bean.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                map.put(field.getName(), field.get(bean));
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private static void mergeReadProperties(
            Object bean, Map<String, Object> map) {
        BeanInfo info = O.getBeanInfo(bean.getClass());

        for (PropertyDescriptor pDesc : info.getPropertyDescriptors()) {
            Method method = pDesc.getReadMethod();
            if (method == null) continue;

            String name = pDesc.getName();
            Optional<Object> value = O.invokeMethod(bean, method);
            if (value.isPresent()) map.put(name, value.get());
        }
    }

    public static Properties toProperties(String properties) {
        Properties result = new Properties();

        try {
            byte[] bytes = properties.getBytes(Charsets.UTF_8);
            result.load(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw Fucks.fuck(e);
        }

        return result;
    }

    public static Properties toProperties(File file) {
        Properties result = new Properties();

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            result.load(fis);
        } catch (IOException e) {
            throw Fucks.fuck(e);
        } finally {
            Closes.closeQuietly(fis);
        }

        return result;
    }

    public static Properties toProperties(InputStream is) {
        Properties result = new Properties();

        try {
            result.load(is);
        } catch (IOException e) {
            throw Fucks.fuck(e);
        }

        return result;
    }
}
