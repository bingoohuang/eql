package org.n3r.eql.eqler.generators;

public class EqlerClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
