package org.n3r.eql.util;

public class Hex {

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String encode(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int temp = (int) b & 0xFF;
            sb.append(HEX_CHARS[temp / 16]);
            sb.append(HEX_CHARS[temp % 16]);
        }
        return sb.toString();
    }

}
