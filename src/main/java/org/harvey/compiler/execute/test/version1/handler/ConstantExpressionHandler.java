package org.harvey.compiler.execute.test.version1.handler;

import org.harvey.compiler.command.CompileProperties;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.ConstantString;
import org.harvey.compiler.execute.expression.LiterallyConstantUtil;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;

/**
 * 常量的处理
 *
 * @date 2025-04-05 15:49
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@SuppressWarnings("unused")
public class ConstantExpressionHandler implements ExpressionHandler {

    @Override
    public boolean handle(ExpressionContext context) {
        ConstantString constantString = constantString(context.next());
        if (constantString == null) {
            context.previousMove();
            return false;
        } else {
            context.add(constantString);
            return true;
        }
    }

    private static ConstantString constantString(SourceString next) {
        SourcePosition position = next.getPosition();
        String value = next.getValue();
        switch (next.getType()) {
            case STRING:
                return new ConstantString(
                        position, LiterallyConstantUtil.stringData(value), ConstantString.ConstantType.STRING);
            case CHAR:
                return new ConstantString(
                        position, LiterallyConstantUtil.charData(value), ConstantString.ConstantType.CHAR);
            case BOOL:
                return new ConstantString(position, boolToBytes(value), ConstantString.ConstantType.BOOL);
            case INT32:
                return new ConstantString(position, numberToBytes(value), ConstantString.ConstantType.INT32);
            case INT64:
                return new ConstantString(position, numberToBytes(value), ConstantString.ConstantType.INT64);
            case FLOAT32:
                return new ConstantString(position, numberToBytes(value), ConstantString.ConstantType.FLOAT32);
            case FLOAT64:
                return new ConstantString(position, numberToBytes(value), ConstantString.ConstantType.FLOAT64);
            default:
                return null;
        }
    }

    private static byte[] boolToBytes(String value) {
        byte[] bytes = new byte[1];
        boolean isTrue = Keyword.TRUE.equals(value);
        boolean isFalse = Keyword.FALSE.equals(value);
        if (isTrue == isFalse) {
            throw new CompilerException(value + " is not a bool");
        }
        bytes[0] = (byte) (isTrue ? 0 : 1);
        return bytes;
    }

    private static byte[] numberToBytes(String value) {
        return value.getBytes(CompileProperties.NUMBER_CHARSET);
    }
}
