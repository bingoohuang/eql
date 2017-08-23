package org.n3r.eql.util;

import com.google.common.base.Charsets;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.convert.todb.EqlToDbConverts;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("unchecked")
public class P {
    public static Map<String, Object> mergeProperties(Map<String, Object> context, Object bean) {
        return MapInvocationHandler.proxy(context, bean);
    }

    public static Object toDbConvert(AccessibleObject accessibleObject, Object value) {
        val converter = EqlToDbConverts.getConverter(accessibleObject);
        if (!converter.isPresent()) return value;

        return converter.get().convert(null, value);
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
