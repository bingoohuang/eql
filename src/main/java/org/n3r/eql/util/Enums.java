package org.n3r.eql.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Modifier;

@UtilityClass
public class Enums {
    @SuppressWarnings("unchecked")
    public Object valueOff(Class<Enum> returnType, String value) {
        val object = invokeValueOff(returnType, value);
        if (object != null) return object;

        return Enum.valueOf(returnType, value);
    }

    private Object invokeValueOff(Class<Enum> returnType, String value) {
        for (val method : returnType.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (!method.getName().equals("valueOff")) continue;
            if (method.getParameterTypes().length != 1) continue;
            if (method.getParameterTypes()[0] != String.class) continue;
            if (!method.getReturnType().isAssignableFrom(returnType)) continue;

            try {
                return method.invoke(null, value);
            } catch (Exception e) {
                // ignore
            }
            break;
        }

        return null;
    }

}

