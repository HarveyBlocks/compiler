package org.harvey.compiler.execute.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.exception.io.CompilerFileWriterException;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO  
 *
 * @date 2025-01-09 04:05
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class LambdaReferenceExpression extends ComplexExpression {
    private final IdentifierString[] arguments;
    /**
     * 1. null 表示是一行的, 其函数体也是表达式, 也能被解析
     * 2. 否则, 去除了{}的内部表达式
     */
    private final int reference;

    public static final ComplexExpression.Type TYPE = Type.LAMBDA;


    public static class Serializer implements StreamSerializer<LambdaReferenceExpression> {
        public static final IdentifierString.Serializer IDENTIFIER_STRING_SERIALIZER = StreamSerializer.get(
                IdentifierString.Serializer.class);

        static {
            ComplexExpression.Serializer.register(TYPE.ordinal(), new LambdaReferenceExpression.Serializer());
        }

        private Serializer() {
        }

        @Override
        public LambdaReferenceExpression in(InputStream is) {
            HeadMap[] head;
            try {
                head = new SerializableData(is.readNBytes(6)).phaseHeader(32, 16);
            } catch (IOException e) {
                throw new CompilerFileWriterException(e);
            }

            int reference = (int) head[0].getValue();
            int argumentsLen = (int) head[1].getValue();
            return new LambdaReferenceExpression(
                    StreamSerializer.readElements(is, argumentsLen, IDENTIFIER_STRING_SERIALIZER)
                            .toArray(IdentifierString[]::new), reference);
        }

        @Override
        public int out(OutputStream os, LambdaReferenceExpression src) {
            byte[] head = Serializes.makeHead(new HeadMap(src.reference, 32).inRange(true, "lambda body reference"),
                    new HeadMap(src.arguments.length, 16).inRange(true, "lambda body reference")).data();
            try {
                os.write(head);
            } catch (IOException e) {
                throw new CompilerFileWriterException(e);
            }
            return head.length + StreamSerializer.writeElements(os, src.arguments, IDENTIFIER_STRING_SERIALIZER);
        }
    }

}
