package org.harvey.compiler.io.serializer.collection;

import org.harvey.compiler.io.serializer.Serializer;
import org.harvey.compiler.io.serializer.structure.EntrySerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 02:32
 */
public class MapSerializer<K, V> implements Serializer<Map<K, V>> {
    private final CollectionSerializer<Map.Entry<K, V>> cs;

    public MapSerializer(InputStream is, OutputStream os,
                         EntrySerializer<K, V> elementSerializer,
                         int sizeBitCount, String elementName) {
        this.cs = new CollectionSerializer<>(is, os, elementSerializer, sizeBitCount, elementName);
    }


    @Override
    public void serialize(Map<K, V> origin) throws IOException {
        cs.serialize(origin.entrySet());
    }

    @Override
    public Map<K, V> deserialize() throws IOException {
        return cs.deserialize().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
