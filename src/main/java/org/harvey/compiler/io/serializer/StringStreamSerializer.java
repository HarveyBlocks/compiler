package org.harvey.compiler.io.serializer;

import org.harvey.compiler.command.CompileProperties;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.exception.io.CompilerFileReadException;
import org.harvey.compiler.exception.io.CompilerFileWriteException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 用于将{@link String}序列化
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-19 17:20
 */
public class StringStreamSerializer implements StreamSerializer<String> {
    public static final int LIMIT_LEN_BIT = 16;

    static {
        StreamSerializerRegister.register(new StringStreamSerializer());
    }

    private StringStreamSerializer() {
    }

    @Override
    public String in(InputStream is) {
        byte[] head;
        try {
            head = is.readNBytes(Serializes.bitCountToByteCount(LIMIT_LEN_BIT));
        } catch (IOException e) {
            throw new CompilerFileReadException(e);
        }
        HeadMap[] headMaps = new SerializableData(head).phaseHeader(LIMIT_LEN_BIT);
        assert headMaps.length == 1;
        int strLen = (int) headMaps[0].getUnsignedValue();
        byte[] data;
        try {
            data = is.readNBytes(strLen);
        } catch (IOException e) {
            throw new CompilerFileReadException(e);
        }
        return new String(data);
    }

    @Override
    public int out(OutputStream os, String src) {
        byte[] data = src.getBytes(CompileProperties.COMPILED_FILE_CHARSET);
        byte[] head = Serializes.makeHead(new HeadMap(data.length, LIMIT_LEN_BIT).inRange(true, "string length"))
                .data();
        try {
            os.write(head);
            os.write(data);
        } catch (IOException e) {
            throw new CompilerFileWriteException(e);
        }
        return head.length + data.length;
    }
}
