package org.n3r.eql.util;

public class Fucks {
    public static class FuckException<T extends Exception> {
        private void pleaseThrow(final Throwable t) throws T {
            throw (T) t;
        }
    }

    static RuntimeException ex = new RuntimeException();

    public static RuntimeException fuck(Throwable t) {
        new FuckException<RuntimeException>().pleaseThrow(t);
        return ex;
    }
}
