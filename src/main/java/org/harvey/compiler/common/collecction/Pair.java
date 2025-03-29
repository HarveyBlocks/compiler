package org.harvey.compiler.common.collecction;

import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * Pair
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-29 22:47
 */
@AllArgsConstructor
public class Pair<K, V> implements Map.Entry<K, V> {
    private final K k;
    private V v;

    @Override
    public K getKey() {
        return k;
    }

    @Override
    public V getValue() {
        return v;
    }

    @Override
    public V setValue(V value) {
        V old = v;
        v = value;
        return old;
    }

    @Override
    public String toString() {
        return k + ": " + v;
    }
}
