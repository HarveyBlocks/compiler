package org.harvey.compiler.io.serializer;

import org.harvey.compiler.common.collecction.Pair;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 用于将{@link Pair}序列化, 效率低, 不建议使用
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-13 15:23
 */
public class PairStreamSerializer<K, V> implements StreamSerializer<Pair<K, V>> {
    private final StreamSerializer<K> ks;
    private final StreamSerializer<V> vs;

    public PairStreamSerializer(StreamSerializer<K> ks, StreamSerializer<V> vs) {
        this.ks = ks;
        this.vs = vs;
    }

    public static <K, V> PairStreamSerializer<K, V> of(Class<StreamSerializer<K>> k, Class<StreamSerializer<V>> v) {
        final StreamSerializer<K> ks = StreamSerializerRegister.get(k);
        final StreamSerializer<V> vs = StreamSerializerRegister.get(v);
        return new PairStreamSerializer<>(ks, vs);
    }

    @Override
    public Pair<K, V> in(InputStream is) {
        K k = ks.in(is);
        V v = vs.in(is);
        return new Pair<>(k, v);
    }

    @Override
    public int out(OutputStream os, Pair<K, V> src) {
        return ks.out(os, src.getKey()) + vs.out(os, src.getValue());
    }
}
