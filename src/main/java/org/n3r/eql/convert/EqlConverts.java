package org.n3r.eql.convert;

import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.map.RsAware;
import org.n3r.eql.util.Ob;
import org.n3r.eql.util.Rs;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class EqlConverts {
    public static Object convertValue(
            RsAware rs, int index,
            Collection<EqlConvertAnn<EqlConvert>> eqlConvertAnns, Object value
    ) {
        if (!eqlConvertAnns.isEmpty()) {
            val originalValue = Rs.getResultSetValue(rs, index, null);
            value = convert(eqlConvertAnns, originalValue);
        }
        return value;
    }

    @SneakyThrows
    private static Object convert(
            Collection<EqlConvertAnn<EqlConvert>> eqlConvertAnns,
            Object value) {
        Object ret = value;

        Method valueMethod = EqlConvert.class.getDeclaredMethod("value");
        for (val eqlConvertAnn : eqlConvertAnns) {
            Class[] clazz = (Class[]) valueMethod.invoke(eqlConvertAnn.convert);

            for (val aClass : clazz) {
                val converter = (EqlConverter) Ob.createInstance(aClass);
                ret = converter.convert(eqlConvertAnn.annotation, ret);
            }
        }

        return ret;
    }

    public static <T extends Annotation> void searchEqlConvertAnns(
            Annotation[] annotations, List<EqlConvertAnn<T>> ecas, Class<T> annClass) {
        for (val annotation : annotations) {
            if (annotation instanceof Retention) continue;
            if (annotation instanceof Target) continue;
            if (annotation instanceof Documented) continue;

            val type = annotation.annotationType();
            T convert = type.getAnnotation(annClass);
            if (convert != null)
                ecas.add(new EqlConvertAnn<T>(convert, annotation));

            val annotations1 = type.getAnnotations();
            searchEqlConvertAnns(annotations1, ecas, annClass);
        }
    }

    public static <T extends Annotation> void searchEqlConvertAnns(
            AccessibleObject accessibleObject,
            List<EqlConvertAnn<T>> ecas,
            Class<T> annClass) {
        searchEqlConvertAnns(accessibleObject.getAnnotations(), ecas, annClass);
    }
}
