package org.harvey.compiler.io.serializer.structure;

import org.harvey.compiler.declare.context.ImportContext;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.serializer.Serializer;
import org.harvey.compiler.io.serializer.collection.ArraySerializer;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * ImportContextSerializer
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 02:16
 */
public class ImportContextSerializer implements Serializer<ImportContext> {


    private final ArraySerializer<SourceString> arraySerializer;

    public ImportContextSerializer(InputStream is, OutputStream os) {
        // 魔数
        this.arraySerializer = new ArraySerializer<>(is, os, new SourceStringSerializer(is, os),
                8, "import path", SourceString[]::new);

    }


    @Override
    public void serialize(ImportContext origin) throws IOException {
        IdentifierString[] path = origin.getPath();
        if (path.length == 0) {
            throw new CompilerException("import path can not be null");
        }
        arraySerializer.serialize(Arrays.stream(path)
                .map(is -> new SourceString(SourceStringType.IDENTIFIER, is.getValue(), is.getPosition()))
                .toArray(SourceString[]::new));
    }

    @Override
    public ImportContext deserialize() throws IOException {
        return new ImportContext(arraySerializer.deserialize());
    }
}
