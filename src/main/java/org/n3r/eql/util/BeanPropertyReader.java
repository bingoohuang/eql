package org.n3r.eql.util;

import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/13.
 */
public class BeanPropertyReader {
    private Class<? extends Object> beanClass;
    private String property;
    private Object bean;
    @Getter private Object propertyValue;
    @Getter private boolean propertyExisted;
    private String capitalizedProperty;

    public BeanPropertyReader(Object bean, String property) {
        if (bean == null) return;

        this.bean = bean;
        this.property = property;
        beanClass = bean.getClass();
        capitalizedProperty = StringUtils.capitalize(property);

        if (bean instanceof Map) {
            propertyValue = ((Map) bean).get(property);
            propertyExisted = propertyValue != null;
        } else {
            if (tryField()) return;
            if (tryGetMethod()) return;
            tryIsMethod();
        }
    }

    private void tryIsMethod() {
        try {
            val isMethod = beanClass.getMethod("is" + capitalizedProperty);
            val value = isMethod.invoke(bean);
            propertyExisted = true;
            propertyValue = P.toDbConvert(isMethod, value);
        } catch (Exception e) {
            // ignore go on
        }
    }

    private boolean tryGetMethod() {
        try {
            val getMethod = beanClass.getMethod("get" + capitalizedProperty);
            val value = getMethod.invoke(bean);
            propertyExisted = true;
            propertyValue = P.toDbConvert(getMethod, value);
            return true;
        } catch (Exception e) {
            // ignore go on
        }
        return false;
    }

    private boolean tryField() {
        try {
            val keyField = beanClass.getDeclaredField(property);
            if (!keyField.isAccessible()) keyField.setAccessible(true);
            val value = keyField.get(bean);
            propertyExisted = true;
            propertyValue = P.toDbConvert(keyField, value);
            return true;
        } catch (Exception e) {
            // ignore, go on
        }
        return false;
    }
}
