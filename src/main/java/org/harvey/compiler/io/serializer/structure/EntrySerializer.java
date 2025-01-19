package org.harvey.compiler.io.serializer.structure;

import org.harvey.compiler.io.serializer.AbstractSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 02:46
 */
public abstract class EntrySerializer<K, V> extends AbstractSerializer<Map.Entry<K, V>> {
    public EntrySerializer(InputStream is, OutputStream os) {
        super(is, os);
    }

    @Override
    public void serialize(Map.Entry<K, V> origin) throws IOException {
        this.serialize(origin.getKey(), origin.getValue());
    }

    protected abstract void serialize(K key, V value) throws IOException;

}
