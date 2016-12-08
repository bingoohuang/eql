package org.n3r.eql.util;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import lombok.SneakyThrows;
import lombok.val;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class C {
    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable e) { // including ClassNotFoundException
            return false;
        }
    }

    public static String getSqlClassPath(int num, String extension) {
        val stackTraceElements = Thread.currentThread().getStackTrace();

        // Remark: when running on IBM jdk (eg. IBM JDK 1.6),
        // the first stackTraceElement will be getStackTraceImpl with
        // lineNo -2 (native method), which add one more stack trace deep than ORACLE/SUN jdk.
        int adjusted = stackTraceElements[0].isNativeMethod() ? 1 : 0;

        String callerClassName = stackTraceElements[num + adjusted].getClassName();
        return callerClassName.replace('.', '/') + "." + extension;
    }

    /*
     * Load a class given its name. BL: We wan't to use a known ClassLoader--hopefully the heirarchy is set correctly.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> tryLoadClass(String className) {
        if (Strings.isNullOrEmpty(className)) return null;

        try {
            return (Class<T>) getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return null;
    }


    /**
     * Return the context classloader. BL: if this is command line operation, the classloading issues are more sane.
     * During servlet execution, we explicitly set the ClassLoader.
     *
     * @return The context classloader.
     */
    public static ClassLoader getClassLoader() {
        return MoreObjects.firstNonNull(
                Thread.currentThread().getContextClassLoader(),
                EqlUtils.class.getClassLoader());
    }


    public static InputStream classResourceToInputStream(String pathname, boolean silent) {
        InputStream is = classResourceToStream(pathname);
        if (is != null || silent) return is;

        throw new RuntimeException("fail to find " + pathname + " in current dir or classpath");
    }

    public static InputStream classResourceToStream(String resourceName) {
        return getClassLoader().getResourceAsStream(resourceName);
    }


    public static boolean classResourceExists(String classPath) {
        URL url = getClassLoader().getResource(classPath);
        return url != null;
    }

    @SneakyThrows
    public static String classResourceToString(String classPath) {
        URL url = getClassLoader().getResource(classPath);
        if (url == null) return null;

        return Resources.toString(url, Charsets.UTF_8);
    }

    @SneakyThrows
    public static List<String> classResourceToLines(String classPath) {
        URL url = getClassLoader().getResource(classPath);
        if (url == null) return null;

        return Resources.readLines(url, Charsets.UTF_8);
    }
}
