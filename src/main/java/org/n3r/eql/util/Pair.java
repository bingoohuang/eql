package org.n3r.eql.util;

import lombok.Data;

@Data
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
}
