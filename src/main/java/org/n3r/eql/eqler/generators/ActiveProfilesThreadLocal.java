package org.n3r.eql.eqler.generators;

public class ActiveProfilesThreadLocal {
    private static ThreadLocal<String[]> local = new ThreadLocal<String[]>();

    public static void set(String[] activeProfiles) {
        local.set(activeProfiles);
    }

    public static String[] get() {
        return local.get();
    }
}
