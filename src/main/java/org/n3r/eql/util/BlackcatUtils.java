package org.n3r.eql.util;

public class BlackcatUtils {
    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable e) { // including ClassNotFoundException
            return false;
        }
    }


    public static boolean HasBlackcat = classExists(
            "com.github.bingoohuang.blackcat.javaagent.callback.Blackcat");

    public static void log(String msgType, String pattern, Object... args) {
        if (!HasBlackcat) return;

        com.github.bingoohuang.blackcat.javaagent.callback
                .Blackcat.log(msgType, pattern, args);
    }
}
