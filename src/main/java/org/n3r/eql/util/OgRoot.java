package org.n3r.eql.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class OgRoot extends HashMap<String, Object> {

    private static final long serialVersionUID = -1;

    public OgRoot() {
        super();
    }

    public OgRoot(Map<? extends String, ? extends Object> m) {
        super(m);
    }

    public boolean isNull(Object target) {
        return target == null;
    }

    public boolean isNotNull(Object target) {
        return target != null;
    }

    public boolean isEmpty(Object target) {
        if (target == null) return true;
        if (target instanceof CharSequence) return ((CharSequence)target).length() == 0;
        if (target instanceof Collection) return ((Collection) target).isEmpty();
        if (target instanceof Map) return ((Map) target).isEmpty();
        if (target instanceof Iterable) return !((Iterable) target).iterator().hasNext();
        if (target.getClass().isArray()) return ((Object[])target).length == 0;

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
}
