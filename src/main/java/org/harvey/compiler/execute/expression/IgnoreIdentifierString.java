package org.harvey.compiler.execute.expression;

import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO  
 *
 * @date 2025-01-08 17:14
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class IgnoreIdentifierString extends ExpressionElement {
    public static final ExpressionElementType TYPE = ExpressionElementType.IGNORE_IDENTIFIER;
    private static final StreamSerializer<IgnoreIdentifierString> SERIALIZER = StreamSerializer.get(
            IgnoreIdentifierString.Serializer.class);

    public IgnoreIdentifierString(SourcePosition position) {
        super(position);
    }

    @Override
    public int out(OutputStream os) {
        return SERIALIZER.out(os, this);
    }

    public static class Serializer implements StreamSerializer<IgnoreIdentifierString> {
        public static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializer.get(
                SourcePosition.Serializer.class);

        static {
            ExpressionElement.Serializer.register(TYPE.ordinal(), new Serializer());
        }

        private Serializer() {
        }

        @Override
        public IgnoreIdentifierString in(InputStream is) {
            SourcePosition sp = SOURCE_POSITION_SERIALIZER.in(is);
            return new IgnoreIdentifierString(sp);
        }

        @Override
        public int out(OutputStream os, IgnoreIdentifierString src) {
            return SOURCE_POSITION_SERIALIZER.out(os, src.getPosition());
        }
    }
}
