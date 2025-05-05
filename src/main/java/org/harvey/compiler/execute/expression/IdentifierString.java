package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StringStreamSerializer;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.type.generic.RawType;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 在表达式中的identifier声明时用到
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:47
 */
@Getter
public class IdentifierString implements RawType, ItemString {
    private static final String IGNORE_VALUE = "";
    private final SourcePosition position;
    /**
     * fullname, separator is .
     */
    private final String value;


    public IdentifierString(SourcePosition sp, String value) {
        this.position = sp;
        this.value = value;
    }


    public IdentifierString(SourceString identifier) {
        this.position = identifier.getPosition();
        if (identifier.getType() == SourceType.IGNORE_IDENTIFIER) {
            this.value = IGNORE_VALUE;
            return;
        }
        if (identifier.getType() != SourceType.IDENTIFIER || identifier.getValue().isEmpty()) {
            throw new AnalysisExpressionException(identifier.getPosition(), "expected a identifier");
        }
        this.value = identifier.getValue();
    }

    /**
     * ignore identifier
     */
    public IdentifierString(SourcePosition position) {
        this(position, IGNORE_VALUE);
    }

    public boolean isIgnore() {
        return value.isEmpty();
    }

    @Override
    public String toString() {
        return this.position + this.value;
    }

    public static class Serializer implements StreamSerializer<IdentifierString> {
        public static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializerRegister.get(
                SourcePosition.Serializer.class);
        public static final StringStreamSerializer STRING_STREAM_SERIALIZER = StreamSerializerRegister.get(
                StringStreamSerializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public IdentifierString in(InputStream is) {
            SourcePosition sp = SOURCE_POSITION_SERIALIZER.in(is);
            String value = STRING_STREAM_SERIALIZER.in(is);
            return new IdentifierString(sp, value);
        }

        @Override
        public int out(OutputStream os, IdentifierString src) {
            return SOURCE_POSITION_SERIALIZER.out(os, src.getPosition()) +
                   STRING_STREAM_SERIALIZER.out(os, src.getValue());
        }
    }
}
