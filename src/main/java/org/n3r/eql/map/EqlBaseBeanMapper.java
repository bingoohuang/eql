package org.n3r.eql.map;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.n3r.eql.ex.EqlExecuteException;
import org.n3r.eql.util.EqlUtils;

public class EqlBaseBeanMapper {
    protected Class<?> mappedClass;
    protected Map<String, PropertyDescriptor> mappedFields;

    public EqlBaseBeanMapper(Class<?> mappedClass) {
        initialize(mappedClass);
    }

    protected void initialize(Class<?> mappedClass) {
        this.mappedClass = mappedClass;
        this.mappedFields = new HashMap<String, PropertyDescriptor>();
        PropertyDescriptor[] pds = getBeanInfo(mappedClass).getPropertyDescriptors();
        for (PropertyDescriptor pd : pds)
            if (pd.getWriteMethod() != null) {
                this.mappedFields.put(pd.getName().toLowerCase(), pd);
                String underscoredName = EqlUtils.underscore(pd.getName());
                if (!pd.getName().toLowerCase().equals(underscoredName)) this.mappedFields.put(underscoredName, pd);
            }
    }

    protected BeanInfo getBeanInfo(Class<?> mappedClass) {
        try {
            return Introspector.getBeanInfo(mappedClass);
        }
        catch (IntrospectionException e) {
            throw new EqlExecuteException(e);
        }
    }
}
