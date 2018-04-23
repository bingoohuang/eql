package org.n3r.eql.util;

import static com.google.common.base.Preconditions.checkState;

/**
 * Key-Value Pair
 */
public class KeyValue {
    private String key;
    private String value;

    public static KeyValue parse(String keyValue) {
        int equalPos = keyValue.indexOf('=');
        if (equalPos < 0) {
            return new KeyValue(keyValue, "");
        }

        checkState(equalPos != 0, keyValue + " is not a valid key value pair (key[=value]");
        String key = keyValue.substring(0, equalPos).trim();
        String value = equalPos < keyValue.length() - 1 ? keyValue.substring(equalPos + 1).trim() : "";
        return new KeyValue(key, value);
    }

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return '{' + key + '=' + value + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyValue keyValue = (KeyValue) o;

        if (key != null ? !key.equals(keyValue.key) : keyValue.key != null) return false;
        return value != null ? value.equals(keyValue.value) : keyValue.value == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public KeyValue removeKeyPrefix(String keyPrefix) {
        String newKey = key.substring(keyPrefix.length());
        if (newKey.startsWith(".")) newKey = newKey.substring(1);
        return new KeyValue(newKey, value);
    }

    public boolean keyStartsWith(String prefix) {
        return key.startsWith(prefix);
    }
}
