package org.n3r.eql.convert;

import org.n3r.eql.map.RsAware;
import org.n3r.eql.util.Fucks;
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
            Object originalValue = Rs.getResultSetValue(rs, index, null);
            value = convert(eqlConvertAnns, originalValue);
        }
        return value;
    }

    private static Object convert(
            Collection<EqlConvertAnn> eqlConvertAnns, Object value
    ) {
        Object ret = value;

        for (EqlConvertAnn eqlConvertAnn : eqlConvertAnns) {
            Class<? extends EqlConverter>[] clazz = eqlConvertAnn.convert.value();

            for (Class<? extends EqlConverter> aClass : clazz) {
                EqlConverter eqlConverter = newInstance(aClass);
                ret = eqlConverter.convert(eqlConvertAnn.annotation, ret);
            }
        }

        return ret;
    }

    private static EqlConverter newInstance(Class<? extends EqlConverter> aClass) {
        try {
            return aClass.newInstance();
        } catch (Exception e) {
            throw Fucks.fuck(e);
        }
    }

    public static void searchEqlConvertAnns(Annotation[] annotations, List<EqlConvertAnn> ecas) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Retention) continue;
            if (annotation instanceof Target) continue;
            if (annotation instanceof Documented) continue;

            Class<? extends Annotation> type = annotation.annotationType();
            EqlConvert convert = type.getAnnotation(EqlConvert.class);
            if (convert != null)
                ecas.add(new EqlConvertAnn(convert, annotation));

            Annotation[] annotations1 = type.getAnnotations();
            searchEqlConvertAnns(annotations1, ecas);
        }
    }

    public static void searchEqlConvertAnns(AccessibleObject accessibleObject, List<EqlConvertAnn> ecas) {
        searchEqlConvertAnns(accessibleObject.getAnnotations(), ecas);
    }
}
