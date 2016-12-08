package org.n3r.eql.convert;

import lombok.SneakyThrows;
import lombok.val;
import org.n3r.eql.map.RsAware;
import org.n3r.eql.util.Rs;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AccessibleObject;
import java.util.Collection;
import java.util.List;

public class EqlConverts {
    public static Object convertValue(
            RsAware rs, int index,
            Collection<EqlConvertAnn> eqlConvertAnns, Object value
    ) {
        if (eqlConvertAnns.size() > 0) {
            val originalValue = Rs.getResultSetValue(rs, index, null);
            value = convert(eqlConvertAnns, originalValue);
        }
        return value;
    }

    private static Object convert(
            Collection<EqlConvertAnn> eqlConvertAnns, Object value
    ) {
        Object ret = value;

        for (val eqlConvertAnn : eqlConvertAnns) {
            val clazz = eqlConvertAnn.convert.value();

            for (val aClass : clazz) {
                val eqlConverter = newInstance(aClass);
                ret = eqlConverter.convert(eqlConvertAnn.annotation, ret);
            }
        }

        return ret;
    }

    @SneakyThrows
    private static EqlConverter newInstance(Class<? extends EqlConverter> aClass) {
        return aClass.newInstance();
    }

    public static void searchEqlConvertAnns(
            Annotation[] annotations, List<EqlConvertAnn> ecas) {
        for (val annotation : annotations) {
            if (annotation instanceof Retention) continue;
            if (annotation instanceof Target) continue;
            if (annotation instanceof Documented) continue;

            val type = annotation.annotationType();
            val convert = type.getAnnotation(EqlConvert.class);
            if (convert != null)
                ecas.add(new EqlConvertAnn(convert, annotation));

            val annotations1 = type.getAnnotations();
            searchEqlConvertAnns(annotations1, ecas);
        }
    }

    public static void searchEqlConvertAnns(
            AccessibleObject accessibleObject, List<EqlConvertAnn> ecas) {
        searchEqlConvertAnns(accessibleObject.getAnnotations(), ecas);
    }
}
