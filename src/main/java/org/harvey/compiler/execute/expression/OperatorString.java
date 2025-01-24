package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.analysis.calculate.Operator;
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
 * @date 2025-01-08 16:46
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class OperatorString extends ExpressionElement {
    public static final ExpressionElementType TYPE = ExpressionElementType.OPERATOR;
    private static final StreamSerializer<OperatorString> SERIALIZER = StreamSerializer.get(
            OperatorString.Serializer.class);
    private final Operator value;

    public OperatorString(SourcePosition sp, Operator value) {
        super(sp);
        this.value = value;
    }

    @Override
    public int out(OutputStream os) {
        return SERIALIZER.out(os, this);
    }

    public static class Serializer implements StreamSerializer<OperatorString> {
        public static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializer.get(
                SourcePosition.Serializer.class);

        static {
            ExpressionElement.Serializer.register(TYPE.ordinal(), new Serializer());
        }

        private Serializer() {
        }

        @Override
        public OperatorString in(InputStream is) {
            SourcePosition sp = SOURCE_POSITION_SERIALIZER.in(is);
            Operator operator;
            try {
                operator = Operator.values()[is.readNBytes(1)[0]];
            } catch (IOException e) {
                throw new CompilerFileReaderException(e);
            }
            return new OperatorString(sp, operator);
        }

        @Override
        public int out(OutputStream os, OperatorString src) {
            return SOURCE_POSITION_SERIALIZER.out(os, src.getPosition()) +
                    writeOneByte(os, (byte) src.getValue().ordinal());
        }
    }

}
