package org.n3r.eql.convert;

import static java.lang.Boolean.parseBoolean;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/2/10.
 */
public class DecodeUtils {

    public static Object decode(String srcStr, String[] decodeValues, String toType) {
        String decodedValue = decode(srcStr, decodeValues);
        if ("boolean".equals(toType)) return parseBoolean(decodedValue);
        return decodedValue;
    }

    public static String decode(String srcStr, String[] decodeValues) {
        int i = 0, ii = decodeValues.length;
        for (; i + 1 < ii; i += 2) {
            if (srcStr.equals(decodeValues[i])) {
                return decodeValues[i + 1];
            }
        }

        return i < ii ? decodeValues[i] : null;
    }
}
