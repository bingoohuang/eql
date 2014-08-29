package org.n3r.eql.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class T {

    /**
     * Examines a Throwable object and gets it's root cause
     *
     * @param t - the exception to examine
     * @return The root cause
     */
    public static Throwable unwrapThrowable(Throwable t) {
        Throwable t2 = t;
        while (true) {
            if (t2 instanceof InvocationTargetException) {
                t2 = ((InvocationTargetException) t).getTargetException();
            } else if (t instanceof UndeclaredThrowableException) {
                t2 = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
            } else {
                return t2;
            }
        }
    }

}
