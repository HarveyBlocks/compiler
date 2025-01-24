package org.harvey.compiler.io.serializer.structure;

import org.harvey.compiler.common.CompileProperties;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.io.serializer.AbstractSerializer;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 12:22
 */
public class StringSerializer extends AbstractSerializer<String> {
    private final int sizeBitCount;
    private final long maxSize;

    public StringSerializer(InputStream is, OutputStream os, int sizeBitCount) {
        super(is, os);
        this.sizeBitCount = sizeBitCount;
        this.maxSize = Serializes.unsignedMaxValue(sizeBitCount);
    }

    @Override
    public void serialize(String origin) throws IOException {
        byte[] data = origin.getBytes(CompileProperties.SOURCE_FILE_CHARSET);
        Serializes.notTooLong(data.length, "string length", maxSize);
        os.write(Serializes.makeHead(new HeadMap(data.length, sizeBitCount)).data());
        os.write(data);
    }

    @Override
    public String deserialize() throws IOException {
        byte[] head = is.readNBytes(Serializes.bitCountToByteCount(sizeBitCount));
        HeadMap[] headMaps = new SerializableData(head).phaseHeader(sizeBitCount);
        assert headMaps.length == 1;
        int strLen = (int) headMaps[0].getValue();
        byte[] data = is.readNBytes(strLen);
        return new String(data);
    }
}
