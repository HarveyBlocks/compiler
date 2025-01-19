package org.harvey.compiler.common;

import java.util.ArrayList;

/**
 * TODO  
 *
 * @date 2025-01-10 15:24
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class PairList<K, V> extends ArrayList<Pair<K, V>> {
    public PairList<K, V> add(K k, V v) {
        this.add(new Pair<>(k, v));
        return this;
    }
}
