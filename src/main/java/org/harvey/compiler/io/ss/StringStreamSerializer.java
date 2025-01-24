package org.harvey.compiler.io.ss;

import org.harvey.compiler.common.CompileProperties;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.exception.io.CompilerFileReaderException;
import org.harvey.compiler.exception.io.CompilerFileWriterException;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO  
 *
 * @date 2025-01-19 17:20
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class StringStreamSerializer implements StreamSerializer<String> {
    public static final int LIMIT_LEN_BIT = 16;

    static {
        StreamSerializer.register(new StringStreamSerializer());
    }

    private StringStreamSerializer() {
    }

    @Override
    public String in(InputStream is) {
        byte[] head;
        try {
            head = is.readNBytes(Serializes.bitCountToByteCount(LIMIT_LEN_BIT));
        } catch (IOException e) {
            throw new CompilerFileReaderException(e);
        }
        HeadMap[] headMaps = new SerializableData(head).phaseHeader(LIMIT_LEN_BIT);
        assert headMaps.length == 1;
        int strLen = (int) headMaps[0].getValue();
        byte[] data;
        try {
            data = is.readNBytes(strLen);
        } catch (IOException e) {
            throw new CompilerFileReaderException(e);
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
            throw new CompilerFileWriterException(e);
        }
        return head.length + data.length;
    }
}
