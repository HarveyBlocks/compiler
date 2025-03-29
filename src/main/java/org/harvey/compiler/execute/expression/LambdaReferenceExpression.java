package org.harvey.compiler.execute.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.exception.io.CompilerFileWriteException;
import org.harvey.compiler.io.serializer.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Lambda表达式的引用, 由于Lambda表达式和函数是一个级别的, 所以引用到别的地方
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-09 04:05
 */
@Getter
@AllArgsConstructor
public class LambdaReferenceExpression extends ComplexExpression {
    public static final ComplexExpression.Type TYPE = Type.LAMBDA;
    private final IdentifierString[] arguments;
    /**
     * 1. null 表示是一行的, 其函数体也是表达式, 也能被解析
     * 2. 否则, 去除了{}的内部表达式
     */
    private final int reference;

    public static class Serializer implements StreamSerializer<LambdaReferenceExpression> {
        public static final IdentifierString.Serializer IDENTIFIER_STRING_SERIALIZER = StreamSerializerRegister.get(
                IdentifierString.Serializer.class);

        static {
            ComplexExpression.Serializer.register(TYPE.ordinal(), new LambdaReferenceExpression.Serializer(),
                    LambdaReferenceExpression.class
            );
        }

        private Serializer() {
        }

        @Override
        public LambdaReferenceExpression in(InputStream is) {
            HeadMap[] head;
            try {
                head = new SerializableData(is.readNBytes(6)).phaseHeader(32, 16);
            } catch (IOException e) {
                throw new CompilerFileWriteException(e);
            }

            int reference = (int) head[0].getUnsignedValue();
            int argumentsLen = (int) head[1].getUnsignedValue();
            return new LambdaReferenceExpression(
                    StreamSerializerUtil.readElements(is, argumentsLen, IDENTIFIER_STRING_SERIALIZER)
                            .toArray(IdentifierString[]::new), reference);
        }

        @Override
        public int out(OutputStream os, LambdaReferenceExpression src) {
            byte[] head = Serializes.makeHead(
                    new HeadMap(src.reference, 32).inRange(true, "lambda body reference"),
                    new HeadMap(src.arguments.length, 16).inRange(true, "lambda body reference")
            ).data();
            try {
                os.write(head);
            } catch (IOException e) {
                throw new CompilerFileWriteException(e);
            }
            return head.length + StreamSerializerUtil.writeElements(os, src.arguments, IDENTIFIER_STRING_SERIALIZER);
        }
    }

}
