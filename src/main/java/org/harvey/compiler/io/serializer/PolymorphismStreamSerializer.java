package org.harvey.compiler.io.serializer;

import org.harvey.compiler.exception.io.CompilerFileReadException;
import org.harvey.compiler.exception.io.CompilerFileWriteException;
import org.harvey.compiler.exception.self.CompilerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * 序列化多态类型的规范, 工具
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-22 01:14
 */
public abstract class PolymorphismStreamSerializer<T extends PolymorphismSerializable> implements StreamSerializer<T> {


    protected PolymorphismStreamSerializer() {
    }

    /**
     * 同时完成 {@link StreamSerializerRegister#register(StreamSerializer)}的任务
     */
    protected static <T extends PolymorphismSerializable> void register(
            Map<Byte, StreamSerializer<? extends T>> serializerMap, byte code,
            StreamSerializer<? extends T> serializer, Map<String, Byte> codeMap, String typeName) {
        StreamSerializer<? extends T> old = serializerMap.get(code);
        if (old != null && old != serializer) {
            throw new CompilerException(
                    "code of " + code + " is repeated with" + old.getClass().getName() + " and " +
                    serializer.getClass().getName());
        }
        serializerMap.put(code, serializer);

        Byte oldCode = codeMap.get(typeName);
        if (oldCode != null && oldCode != code) {
            throw new CompilerException(
                    "type of " + typeName + " is repeated with code" + oldCode + " and " + code);
        }
        codeMap.put(typeName, code);
        StreamSerializerRegister.register(serializer);
    }


    protected static <T extends PolymorphismSerializable> T in(
            Map<Byte, StreamSerializer<? extends T>> map,
            InputStream is) {
        byte code;
        try {
            code = is.readNBytes(1)[0];
        } catch (IOException e) {
            throw new CompilerFileReadException(e);
        }
        T element = map.get(code).in(is);
        if (element == null) {
            throw new CompilerException("Unknown Expression Element OnlyFileStatementSerializer");
        }
        return element;
    }

    protected static <T extends PolymorphismSerializable> int out(Map<String, Byte> map, OutputStream os, T src) {
        try {
            os.write(new byte[]{map.get(src.getClass().getName())});
        } catch (IOException e) {
            throw new CompilerFileWriteException(e);
        }
        return 1 + src.out(os);
    }

}
