package org.n3r.eql.map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import lombok.val;
import org.n3r.eql.convert.EqlConvert;
import org.n3r.eql.convert.EqlConvertAnn;
import org.n3r.eql.convert.EqlConverts;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.util.Names;
import org.n3r.eql.util.O;
import org.n3r.eql.util.Rs;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class EqlBaseBeanMapper {
    protected Class<?> mappedClass;
    protected Map<String, PropertyDescriptor> mappedProperties;
    protected Map<String, Field> mappedFields;
    protected Multimap<String, EqlConvertAnn<EqlConvert>> converters = HashMultimap.create();

    public EqlBaseBeanMapper(Class<?> mappedClass) {
        initialize(mappedClass);
    }

    protected void initialize(Class<?> mappedClass) {
        this.mappedClass = mappedClass;
        this.mappedProperties = Maps.newHashMap();
        val pds = O.getBeanInfo(mappedClass).getPropertyDescriptors();
        for (val pd : pds) {
            if (pd.getWriteMethod() != null) {
                this.mappedProperties.put(pd.getName().toLowerCase(), pd);
                String underscoredName = Names.underscore(pd.getName());
                if (!pd.getName().toLowerCase().equals(underscoredName))
                    this.mappedProperties.put(underscoredName, pd);
            }
        }

        mappedFields = Maps.newHashMap();
        for (Field field : mappedClass.getDeclaredFields()) {
            mappedFields.put(field.getName().toLowerCase(), field);
            List<EqlConvertAnn<EqlConvert>> ecas = Lists.newArrayList();
            EqlConverts.searchEqlConvertAnns(field, ecas, EqlConvert.class);
            if (!ecas.isEmpty()) converters.putAll(field.getName(), ecas);
        }
    }

    protected boolean setColumnValue(
            final RsAware rs, Object mappedObject,
            final int index, String columnName) {
        val lowerCaseName = columnName.replaceAll(" ", "").toLowerCase();
        val none_ = lowerCaseName.replaceAll("_", "");

        PropertyDescriptor pd = this.mappedProperties.get(lowerCaseName);
        if (pd == null) pd = this.mappedProperties.get(none_);
        if (pd != null) {
            val eqlConvertAnns = converters.get(pd.getName());
            Object value;
            if (!eqlConvertAnns.isEmpty()) {
                val originalValue = Rs.getResultSetValue(rs, index, null);
                value = EqlConverts.convert(eqlConvertAnns, originalValue);
            } else {
                value = Rs.getResultSetValue(rs, index, pd.getPropertyType());
            }

            boolean succ = O.setProperty(mappedObject, pd, value);
            if (succ) return true;
        }

        Field field = this.mappedFields.get(lowerCaseName);
        if (field == null) field = this.mappedFields.get(none_);
        if (field != null) {
            val eqlConvertAnns = converters.get(field.getName());
            Object value;
            if (!eqlConvertAnns.isEmpty()) {
                val originalValue = Rs.getResultSetValue(rs, index, null);
                value = EqlConverts.convert(eqlConvertAnns, originalValue);
            } else {
                value = Rs.getResultSetValue(rs, index, field.getType());
            }

            Reflect.on(mappedObject).set(field.getName(), value);
            return true;
        }

        return O.setValue(mappedObject, columnName, new O.ValueGettable() {
            @Override
            public Object getValue() {
                return Rs.getResultSetValue(rs, index);
            }

            @Override
            public Object getValue(Class<?> returnType) {
                return Rs.getResultSetValue(rs, index, returnType);
            }
        });
    }

}
