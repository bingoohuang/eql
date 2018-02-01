package org.n3r.eql.eqler.generators;

import org.springframework.context.ApplicationContext;

public class ApplicationContextThreadLocal {
    private static ThreadLocal<ApplicationContext> local = new ThreadLocal<ApplicationContext>();

    public static void set(ApplicationContext applicationContext) {
        local.set(applicationContext);
    }

    public static ApplicationContext get() {
        return local.get();
    }
}
