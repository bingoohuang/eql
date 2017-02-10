package org.n3r.eql.util;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.base.EqlToProperties;
import org.n3r.eql.convert.todb.EqlToDbConverts;

import java.beans.BeanInfo;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
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
            mergeReadProperties(bean, map);
            mergeDeclaredFields(bean, map);
        }

        return map;
    }

    private static void mergeDeclaredFields(
            Object bean, Map<String, Object> map) {
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

    private static Object toDbConvert(AccessibleObject accessibleObject, Object value) {
        val converter = EqlToDbConverts.getConverter(accessibleObject);
        if (!converter.isPresent()) return value;

        return converter.get().convert(null, value);
    }

    private static void mergeReadProperties(
            Object bean, Map<String, Object> map) {
        BeanInfo info = O.getBeanInfo(bean.getClass());

        for (val pDesc : info.getPropertyDescriptors()) {
            Method method = pDesc.getReadMethod();
            if (method == null) continue;

            String name = pDesc.getName();
            Optional<Object> value = O.invokeMethod(bean, method);
            if (value.isPresent()) {
                Object val = value.get();
                val = toDbConvert(method, val);
                map.put(name, val);
            }
        }
    }

    @SneakyThrows
    public static Properties toProperties(String properties) {
        Properties result = new Properties();

        byte[] bytes = properties.getBytes(Charsets.UTF_8);
        result.load(new ByteArrayInputStream(bytes));

        return result;
    }

    @SneakyThrows
    public static Properties toProperties(File file) {
        Properties result = new Properties();

        @Cleanup FileInputStream fis = new FileInputStream(file);
        result.load(fis);

        return result;
    }

    @SneakyThrows
    public static Properties toProperties(InputStream is) {
        Properties result = new Properties();
        result.load(is);
        return result;
    }
}
