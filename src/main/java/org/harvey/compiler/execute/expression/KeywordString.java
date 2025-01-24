package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.exception.io.CompilerFileReaderException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.harvey.compiler.io.ss.StreamSerializer.writeOneByte;

/**
 * TODO
 *
 * @date 2025-01-08 16:49
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class KeywordString extends ExpressionElement {
    public static final ExpressionElementType TYPE = ExpressionElementType.KEYWORD;
    private static final StreamSerializer<KeywordString> SERIALIZER = StreamSerializer.get(
            KeywordString.Serializer.class);
    // 大端
    private final Keyword keyword;

    public KeywordString(SourcePosition sp, Keyword keyword) {
        super(sp);
        this.keyword = keyword;
    }

    @Override
    public int out(OutputStream os) {
        return SERIALIZER.out(os, this);
    }

    public static class Serializer implements StreamSerializer<KeywordString> {
        public static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializer.get(
                SourcePosition.Serializer.class);

        static {
            ExpressionElement.Serializer.register(TYPE.ordinal(), new Serializer());
        }

        private Serializer() {
        }

        @Override
        public KeywordString in(InputStream is) {
            SourcePosition sp = SOURCE_POSITION_SERIALIZER.in(is);
            Keyword keyword;
            try {
                keyword = Keyword.values()[is.readNBytes(1)[0]];
            } catch (IOException e) {
                throw new CompilerFileReaderException(e);
            }
            return new KeywordString(sp, keyword);
        }

        @Override
        public int out(OutputStream os, KeywordString src) {
            return SOURCE_POSITION_SERIALIZER.out(os, src.getPosition()) +
                    writeOneByte(os, (byte) src.getKeyword().ordinal());
        }
    }
}
