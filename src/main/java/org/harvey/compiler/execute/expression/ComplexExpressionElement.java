package org.harvey.compiler.execute.expression;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 例如lambda表达式之类的
 */
@Getter
@Setter
public class ComplexExpressionElement extends ExpressionElement {
    public static final ExpressionElementType TYPE = ExpressionElementType.COMPLEX;

    private ComplexExpression expression;

    public ComplexExpressionElement(SourcePosition sp, ComplexExpression expression) {
        super(sp);
        this.expression = expression;
    }

    private static final StreamSerializer<ComplexExpressionElement> SERIALIZER = StreamSerializer.get(
            ComplexExpressionElement.Serializer.class);

    @Override
    public int out(OutputStream os) {
        return SERIALIZER.out(os, this);
    }

    public static class Serializer implements StreamSerializer<ComplexExpressionElement> {
        public static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializer.get(
                SourcePosition.Serializer.class);
        public static final ComplexExpression.Serializer COMPLEX_EXPRESSION_SERIALIZER = StreamSerializer.get(
                ComplexExpression.Serializer.class);

        static {
            ExpressionElement.Serializer.register(TYPE.ordinal(), new Serializer());
        }

        private Serializer() {
        }

        @Override
        public ComplexExpressionElement in(InputStream is) {
            SourcePosition sp = SOURCE_POSITION_SERIALIZER.in(is);
            ComplexExpression complexExpression = COMPLEX_EXPRESSION_SERIALIZER.in(is);
            return new ComplexExpressionElement(sp,complexExpression);
        }

        @Override
        public int out(OutputStream os, ComplexExpressionElement src) {
            return SOURCE_POSITION_SERIALIZER.out(os, src.getPosition()) +
                    COMPLEX_EXPRESSION_SERIALIZER.out(os, src.getExpression());
        }
    }
}
