package org.n3r.eql.util;

import com.google.common.base.CaseFormat;

public class Names {
    public static String underscoreNameToPropertyName(String name) {
        StringBuilder result = new StringBuilder();
        boolean nextIsUpper = false;
        if (name != null && name.length() > 0) {
            if (name.length() > 1 && name.substring(1, 2).equals("_")) {
                result.append(name.substring(0, 1).toUpperCase());
            } else {
                result.append(name.substring(0, 1).toLowerCase());
            }
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                if (s.equals("_")) {
                    nextIsUpper = true;
                } else {
                    if (nextIsUpper) {
                        result.append(s.toUpperCase());
                        nextIsUpper = false;
                    } else {
                        result.append(s.toLowerCase());
                    }
                }
            }
        }
        return result.toString();
    }

    public static String convertCamelToUnderscore(String className) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className);
    }

    /**
     * Convert a name in camelCase to an underscored name in lower case.
     * Any upper case letters are converted to lower case with a preceding underscore.
     *
     * @param name the string containing original name
     * @return the converted name
     */
    public static String underscore(String name) {
        StringBuilder result = new StringBuilder();
        if (name != null && name.length() > 0) {
            result.append(name.substring(0, 1).toLowerCase());
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                if (s.equals(s.toUpperCase())) {
                    result.append("_");
                    result.append(s.toLowerCase());
                } else result.append(s);
            }
        }

        return result.toString();
    }
}
