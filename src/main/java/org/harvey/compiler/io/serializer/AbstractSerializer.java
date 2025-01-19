package org.harvey.compiler.io.serializer;

import lombok.AllArgsConstructor;
import org.harvey.compiler.common.CompileProperties;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 序列化与反序列化
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-29 21:53
 */
@AllArgsConstructor
public abstract class AbstractSerializer<T> implements Serializer<T> {
    protected final InputStream is;
    protected final OutputStream os;


    protected static String getString(SerializableData read) {
        return new String(read.data(), CompileProperties.SOURCE_FILE_CHARSET);
    }
}
