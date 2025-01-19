package org.harvey.compiler.io.serializer.collection;

import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.io.serializer.AbstractSerializer;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.harvey.compiler.io.serializer.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 01:50
 */
public class CollectionSerializer<E> extends AbstractSerializer<Collection<E>> {
    private final Serializer<E> es;
    private final int sizeBitCount;
    private final String elementName;

    public CollectionSerializer(
            InputStream is, OutputStream os,
            Serializer<E> elementSerializer,
            int sizeBitCount, String elementName) {
        super(is, os);
        this.es = elementSerializer;
        this.sizeBitCount = sizeBitCount;
        this.elementName = elementName;
    }

    @Override
    public void serialize(Collection<E> origin) throws IOException {
        // import的个数
        Serializes.notTooMuch(origin.size(), elementName, Serializes.maxValue(sizeBitCount));
        os.write(Serializes.makeHead(new HeadMap(origin.size(), sizeBitCount)).data());
        for (E value : origin) {
            es.serialize(value);
        }
    }

    @Override
    public Collection<E> deserialize() throws IOException {
        SerializableData head = new SerializableData(
                is.readNBytes(Serializes.bitCountToByteCount(sizeBitCount)));
        HeadMap[] headMaps = head.phaseHeader(sizeBitCount);
        assert headMaps.length == 1;
        long size = headMaps[0].getValue();
        List<E> result = new ArrayList<>((int) size);
        while (size-- > 0) {
            result.add(es.deserialize());
        }
        return result;
    }
}
