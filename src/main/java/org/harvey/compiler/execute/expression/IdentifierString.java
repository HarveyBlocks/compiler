package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO
 *
 * @date 2025-01-08 16:47
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class IdentifierString extends ExpressionElement {
    private final String value;

    public IdentifierString(SourcePosition sp, String value) {
        super(sp);
        this.value = value;
    }

    public IdentifierString(SourceString identifier) {
        super(identifier.getPosition());
        if (identifier.getType() != SourceStringType.IDENTIFIER) {
            throw new AnalysisExpressionException(identifier.getPosition(), "expected a identifier");
        }
        this.value = identifier.getValue();
    }

    public SourceString toSource() {
        return new SourceString(SourceStringType.IDENTIFIER, value, super.getPosition());
    }

    public static class Serializer implements StreamSerializer<IdentifierString> {
        static {
            StreamSerializer.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public IdentifierString in(InputStream is) {
            return null;
        }

        @Override
        public int out(OutputStream os, IdentifierString src) {
            return 0;
        }
    }
}
