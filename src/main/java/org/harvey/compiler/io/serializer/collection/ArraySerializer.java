package org.harvey.compiler.io.serializer.collection;

import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.io.serializer.AbstractSerializer;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.harvey.compiler.io.serializer.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 02:01
 */
public class ArraySerializer<E> extends AbstractSerializer<E[]> {
    private final Serializer<E> es;
    private final int sizeBitCount;
    private final String elementName;
    private final Function<Integer, E[]> arrayInit;

    public ArraySerializer(
            InputStream is, OutputStream os,
            Serializer<E> elementSerializer,
            int sizeBitCount, String elementName, Function<Integer, E[]> arrayInit) {
        super(is, os);
        this.es = elementSerializer;
        this.sizeBitCount = sizeBitCount;
        this.elementName = elementName;
        this.arrayInit = arrayInit;
    }

    @Override
    public void serialize(E[] origin) throws IOException {
        // import的个数
        Serializes.notTooMuch(origin.length, elementName, Serializes.maxValue(sizeBitCount));
        os.write(Serializes.makeHead(new HeadMap(origin.length, sizeBitCount)).data());
        for (E value : origin) {
            es.serialize(value);
        }
    }

    @Override
    public E[] deserialize() throws IOException {
        SerializableData head = new SerializableData(
                is.readNBytes(Serializes.bitCountToByteCount(sizeBitCount)));
        HeadMap[] headMaps = head.phaseHeader(sizeBitCount);
        assert headMaps.length == 1;
        long size = headMaps[0].getValue();
        // 肯定不对
        E[] result = arrayInit.apply((int) size);
        for (int i = 0; i < size; i++) {
            result[i] = es.deserialize();
        }
        return result;
    }
}
