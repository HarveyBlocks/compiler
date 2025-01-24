package org.harvey.compiler.declare.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.common.SourceFileConstant;
import org.harvey.compiler.common.SystemConstant;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.io.CompilerFileReaderException;
import org.harvey.compiler.exception.io.CompilerFileWriterException;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Import的信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:40
 */
@Getter
@AllArgsConstructor
public class ImportContext {
    // 用`.`分割
    private final IdentifierString[] path;
    private final SourcePosition position;

    public ImportContext(SourceString[] path) {
        if (path.length == 0) {
            throw new CompilerException("Need import package");
        }
        this.position = path[0].getPosition();
        this.path = Arrays.stream(path).map(IdentifierString::new).toArray(IdentifierString[]::new);
    }

    public IdentifierString getTarget() {
        return path[path.length - 1];
    }

    public String[] getStringPath() {
        return Arrays.stream(path).map(IdentifierString::getValue).toArray(String[]::new);
    }

    public String getPackagePath() {
        return concatPath(String.valueOf(SourceFileConstant.PACKAGE_SEPARATOR));
    }


    public String getDictionaryPath() {
        return concatPath(SystemConstant.FILE_SEPARATOR);
    }

    private String concatPath(String separator) {
        StringBuilder sb = new StringBuilder();
        sb.append(path[0].getValue());
        for (int i = 1; i < path.length; i++) {
            sb.append(separator).append(path[i].getValue());
        }
        return sb.toString();
    }

    public static class Serializer implements StreamSerializer<ImportContext> {
        public static final int PATH_SIZE_LIMIT = 8;
        public static final int PATH_SIZE_LIMIT_BYTE = Serializes.bitCountToByteCount(PATH_SIZE_LIMIT);
        private static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializer.get(
                SourcePosition.Serializer.class);
        private static final IdentifierString.Serializer IDENTIFIER_STRING_SERIALIZER = StreamSerializer.get(
                IdentifierString.Serializer.class);

        static {
            StreamSerializer.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public ImportContext in(InputStream is) {
            SourcePosition position = SOURCE_POSITION_SERIALIZER.in(is);
            byte[] head;
            try {
                head = is.readNBytes(PATH_SIZE_LIMIT_BYTE);
            } catch (IOException e) {
                throw new CompilerFileReaderException(e);
            }
            HeadMap[] headMap = new SerializableData(head).phaseHeader(PATH_SIZE_LIMIT);
            IdentifierString[] path = StreamSerializer.readElements(is, headMap[0].getValue(),
                    IDENTIFIER_STRING_SERIALIZER).toArray(IdentifierString[]::new);
            return new ImportContext(path, position);
        }

        @Override
        public int out(OutputStream os, ImportContext src) {
            SourcePosition position = src.position;
            IdentifierString[] path = src.path;
            int positionLen = SOURCE_POSITION_SERIALIZER.out(os, position);
            byte[] head = Serializes.makeHead(
                    new HeadMap(path.length, PATH_SIZE_LIMIT).inRange(true,"import path length")
            ).data();
            try {
                os.write(head);
            } catch (IOException e) {
                throw new CompilerFileWriterException(e);
            }
            return positionLen + head.length + StreamSerializer.writeElements(os, path, IDENTIFIER_STRING_SERIALIZER);
        }
    }
}
