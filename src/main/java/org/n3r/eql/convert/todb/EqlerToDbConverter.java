package org.n3r.eql.convert.todb;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.val;
import org.n3r.eql.convert.EqlConvertAnn;
import org.n3r.eql.util.Ob;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/10.
 */
@AllArgsConstructor
public class EqlerToDbConverter implements ToDbConverter {
    Annotation annotation;
    List<ToDbConverter> converters;

    public EqlerToDbConverter() {
        this(null, Lists.newArrayList());
    }

    @Override public Object convert(Annotation ann, Object src) {
        Object result = src;
        for (ToDbConverter converter : converters) {
            result = converter.convert(annotation, result);
        }

        return result;
    }

    public void addConvertAnn(EqlConvertAnn<ToDbConvert> eca) {
        val classes = eca.convert.value();
        List<ToDbConverter> subConverters = Lists.newArrayListWithCapacity(classes.length);
        for (val clazz : classes) {
            val toDbConverter = (ToDbConverter) Ob.createInstance(clazz);
            subConverters.add(toDbConverter);
        }

        converters.add(new EqlerToDbConverter(eca.annotation, subConverters));
    }
}
