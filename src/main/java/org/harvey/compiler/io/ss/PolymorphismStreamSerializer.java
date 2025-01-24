package org.harvey.compiler.io.ss;

import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.io.CompilerFileReaderException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * TODO  
 *
 * @date 2025-01-22 01:14
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public abstract class PolymorphismStreamSerializer<T extends PolymorphismSerializable> implements StreamSerializer<T> {


    protected PolymorphismStreamSerializer() {
    }

    /**
     * 同时完成 {@link StreamSerializer#register(StreamSerializer)}的任务
     */
    protected static <T extends PolymorphismSerializable> void register(Map<Integer, StreamSerializer<? extends T>> map,
                                                                        int code,
                                                                        StreamSerializer<? extends T> serializer) {
        if (map.containsKey(code)) {
            throw new CompilerException("code of " + code + " is repeated with" + map.get(code));
        }
        map.put(code, serializer);
        StreamSerializer.register(serializer);
    }


    protected static <T extends PolymorphismSerializable> T in(Map<Integer, StreamSerializer<? extends T>> map,
                                                               InputStream is) {
        int code;
        try {
            code = is.readNBytes(1)[0];
        } catch (IOException e) {
            throw new CompilerFileReaderException(e);
        }
        T element = map.get(code).in(is);
        if (element == null) {
            throw new CompilerException("Unknown Expression Element Serializer");
        }
        return element;
    }

    @Override
    public int out(OutputStream os, T src) {
        return src.out(os);
    }
}
