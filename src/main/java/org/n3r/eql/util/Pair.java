package org.n3r.eql.util;

public class Pair<K, V> {
    public K _1;
    public V _2;

    public Pair(K _1, V _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public static <K, V> Pair<K, V> of(K k, V v) {
        return new Pair<K, V>(k, v);
    }

    @Override
    public String toString() {
        return "{" + _1 + "," + _2 + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (_1 != null ? !_1.equals(pair._1) : pair._1 != null) return false;
        return _2 != null ? _2.equals(pair._2) : pair._2 == null;
    }

    @Override
    public int hashCode() {
        int result = _1 != null ? _1.hashCode() : 0;
        result = 31 * result + (_2 != null ? _2.hashCode() : 0);
        return result;
    }
}
