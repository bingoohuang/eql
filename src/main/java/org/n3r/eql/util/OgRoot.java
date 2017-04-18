package org.n3r.eql.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class OgRoot implements Map<String, Object>  {
    private final Map map;

    public OgRoot(Map<String, Object> m) {
        this.map = m;
    }

    public boolean isNull(Object target) {
        return target == null;
    }

    public boolean isNotNull(Object target) {
        return target != null;
    }

    public boolean isEmpty(Object target) {
        if (target == null) return true;
        if (target instanceof CharSequence) return ((CharSequence) target).length() == 0;
        if (target instanceof Collection) return ((Collection) target).isEmpty();
        if (target instanceof Map) return ((Map) target).isEmpty();
        if (target instanceof Iterable) return !((Iterable) target).iterator().hasNext();
        if (target.getClass().isArray()) return ((Object[]) target).length == 0;

        return false;
    }

    public boolean isNotEmpty(Object target) {
        return !isEmpty(target);
    }

    public boolean isBlank(Object target) {
        return isEmpty(target) || target.toString().trim().length() == 0;
    }

    public boolean isNotBlank(Object target) {
        return !isBlank(target);
    }

    public boolean alike(Object object1, Object object2) {
        if (object1 == object2) return true;
        if (object1 == null || object2 == null) return false;
        return object1.equals(object2) || object1.toString().equals(object2.toString());
    }

    @Override public int size() {
        return map.size();
    }

    @Override public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override public Object get(Object key) {
        return map.get(key);
    }

    @Override public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override public Object remove(Object key) {
        return map.remove(key);
    }

    @Override public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    @Override public void clear() {
        map.clear();
    }

    @Override public Set<String> keySet() {
        return map.keySet();
    }

    @Override public Collection<Object> values() {
        return map.values();
    }

    @Override public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }
}
