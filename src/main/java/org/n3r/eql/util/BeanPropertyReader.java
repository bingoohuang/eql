package org.n3r.eql.util;

import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/13.
 */
public class BeanPropertyReader {
    private Class<? extends Object> beanClass;
    private String property;
    private Object bean;
    private Object propertyValue;
    private boolean hasProperty;
    private String capitalizedProperty;

    public BeanPropertyReader(Object bean, String property) {
        if (bean == null) return;

        this.bean = bean;
        this.property = property;
        beanClass = bean.getClass();
        capitalizedProperty = StringUtils.capitalize(property);

        if (bean instanceof Map) {
            val map = (Map) bean;
            propertyValue = map.get(property);
            hasProperty = propertyValue != null;
        }
        if (tryField()) return;
        if (tryGetMethod()) return;
        tryIsMethod();
    }

    private void tryIsMethod() {
        try {

            val isMethod = beanClass.getMethod("is" + capitalizedProperty);
            hasProperty = true;
            val value = isMethod.invoke(bean);
            propertyValue = P.toDbConvert(isMethod, value);
        } catch (Exception e) {
            // ignore go on
        }
    }

    private boolean tryGetMethod() {
        try {
            val getMethod = beanClass.getMethod("get" + StringUtils.capitalize(property));
            hasProperty = true;
            val value = getMethod.invoke(bean);
            propertyValue = P.toDbConvert(getMethod, value);
            return true;
        } catch (Exception e) {
            // ignore go on
        }
        return false;
    }

    private boolean tryField() {
        try {
            Field keyField = beanClass.getDeclaredField(property);
            if (!keyField.isAccessible()) keyField.setAccessible(true);
            hasProperty = true;
            Object value = keyField.get(bean);
            propertyValue = P.toDbConvert(keyField, value);
            return true;
        } catch (Exception e) {
            // ignore, go on
        }
        return false;
    }

    public boolean existsProperty() {
        return hasProperty;
    }

    public Object readProperty() {
        return propertyValue;
    }
}
