package org.harvey.compiler.execute.control;

import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.local.LocalType;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 23:25
 */
public abstract class Executable {

    protected static final ExpressionElement.Serializer EE_S = StreamSerializerRegister.get(
            ExpressionElement.Serializer.class);
    protected static final IdentifierString.Serializer IS_S = StreamSerializerRegister.get(
            IdentifierString.Serializer.class);
    protected static final LocalType.Serializer LT_S = StreamSerializerRegister.get(
            LocalType.Serializer.class);

    protected static int writeInteger(OutputStream os, int num) {
        return StreamSerializerUtil.writeNumber(os, num, 32, true);
    }

    protected static int readInteger(InputStream is) {
        return (int) StreamSerializerUtil.readNumber(is, 32, true);
    }

    protected static int writeExpression(OutputStream os, Expression expression) {
        return StreamSerializerUtil.collectionOut(os, expression, EE_S);
    }

    protected static Expression readExpression(InputStream is) {
        return new Expression(StreamSerializerUtil.collectionIn(is, EE_S));
    }

    public abstract byte getCode();

    public abstract int out(OutputStream os);
}
