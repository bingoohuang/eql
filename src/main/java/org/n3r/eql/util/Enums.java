package org.n3r.eql.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Enums {
    public static Object valueOff(Class<Enum> returnType, String value) {
        Object object = invokeValueOff(returnType, value);
        if (object != null) return object;

        return Enum.valueOf(returnType, value);
    }

    private static Object invokeValueOff(Class<Enum> returnType, String value) {
        try {
            Method valueOffMethod = returnType.getMethod("valueOff", new Class<?>[]{String.class});
            if (valueOffMethod != null && Modifier.isStatic(valueOffMethod.getModifiers())
                    && valueOffMethod.getReturnType().isAssignableFrom(returnType)) {
                return valueOffMethod.invoke(null, value);
            }

        } catch (Exception e) {
            // ignore
        }

        return null;
    }

}

