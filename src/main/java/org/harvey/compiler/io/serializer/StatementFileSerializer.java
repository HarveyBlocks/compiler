package org.harvey.compiler.io.serializer;


import org.harvey.compiler.exception.io.CompilerFileReadException;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.stage.CompileStage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.harvey.compiler.io.serializer.StreamSerializerUtil.collectionIn;
import static org.harvey.compiler.io.serializer.StreamSerializerUtil.collectionOut;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-23 20:35
 */
public interface StatementFileSerializer {
    SourceStringStreamSerializer SOURCE_STRING_SERIALIZER = StreamSerializerRegister.get(
            SourceStringStreamSerializer.class);

    static void skip(InputStream is, int skip) {
        try {
            if (is.skip(skip) != skip) {
                throw new IOException("can not skip: " + skip);
            }
        } catch (IOException e) {
            throw new CompilerFileReadException(e);
        }
    }

    static int writePool(OutputStream os, List<? extends List<SourceString>> executablePool) {
        int len = 0;
        for (List<SourceString> src : executablePool) {
            len += collectionOut(os, src, SOURCE_STRING_SERIALIZER);
        }
        return len;
    }

    static List<ArrayList<SourceString>> readPool(InputStream is, int len) {
        List<ArrayList<SourceString>> result = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            result.add(collectionIn(is, SOURCE_STRING_SERIALIZER));
        }
        return result;
    }

    File getFile();

    Object getResource();

    void updateStage(CompileStage stage);

    /**
     * {@link CompileStage#STATEMENT} {@link CompileStage#LINKING} {@link CompileStage#COMPILED}
     */
    Object in(InputStream is);
}
