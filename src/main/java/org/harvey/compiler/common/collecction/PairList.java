package org.harvey.compiler.common.collecction;

import java.util.ArrayList;

/**
 * 用Pair组成的List
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-10 15:24
 */
public class PairList<K, V> extends ArrayList<Pair<K, V>> {
    public PairList<K, V> add(K k, V v) {
        this.add(new Pair<>(k, v));
        return this;
    }
}
