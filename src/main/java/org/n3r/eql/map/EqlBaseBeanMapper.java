package org.n3r.eql.map;

import com.google.common.collect.Maps;
import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.joor.Reflect;
import org.n3r.eql.util.Names;
import org.n3r.eql.util.Rs;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

public class EqlBaseBeanMapper {
    protected Class<?> mappedClass;
    protected Map<String, PropertyDescriptor> mappedProperties;
    protected Map<String, Field> mappedFields;

    public EqlBaseBeanMapper(Class<?> mappedClass) {
        initialize(mappedClass);
    }

    protected void initialize(Class<?> mappedClass) {
        this.mappedClass = mappedClass;
        this.mappedProperties = Maps.newHashMap();
        PropertyDescriptor[] pds = getBeanInfo(mappedClass).getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null) {
                this.mappedProperties.put(pd.getName().toLowerCase(), pd);
                String underscoredName = Names.underscore(pd.getName());
                if (!pd.getName().toLowerCase().equals(underscoredName)) this.mappedProperties.put(underscoredName, pd);
            }
        }

        mappedFields = Maps.newHashMap();
        Field[] declaredFields = mappedClass.getDeclaredFields();
        for (Field field : declaredFields) {
            mappedFields.put(field.getName().toLowerCase(), field);
        }
    }

    protected BeanInfo getBeanInfo(Class<?> mappedClass) {
        try {
            return Introspector.getBeanInfo(mappedClass);
        } catch (IntrospectionException e) {
            throw new EqlExecuteException(e);
        }
    }

    protected void setColumnValue(RsAware rs, Object mappedObject, int index, String columnName) throws SQLException {
        String lowerCaseName = columnName.replaceAll(" ", "").replaceAll("_", "").toLowerCase();
        PropertyDescriptor pd = this.mappedProperties.get(lowerCaseName);
        boolean setSucc = false;
        if (pd != null) {
            Object value = Rs.getResultSetValue(rs, index, pd.getPropertyType());
            setSucc = setBeanProperty(mappedObject, pd, value);
        }
        if (!setSucc) {
            Field field = this.mappedFields.get(lowerCaseName);
            if (field != null) {
                Object value = Rs.getResultSetValue(rs, index, field.getType());
                Reflect.on(mappedObject).set(field.getName(), value);
            }
        }
    }

    protected boolean setBeanProperty(Object object, PropertyDescriptor pd, Object value) {
        Method setter = pd.getWriteMethod();
        if (setter == null) return false;

        try {
            setter.invoke(object, value);
            return true;
        } catch (Exception e) {

        }

        return false;
    }
}
