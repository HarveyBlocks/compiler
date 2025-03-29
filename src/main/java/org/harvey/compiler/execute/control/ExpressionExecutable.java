package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.Expression;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-10 13:22
 */
@Getter
@AllArgsConstructor
public class ExpressionExecutable extends SequentialExecutable {
    public static final Byte CODE = 11;
    private final Expression expression;

    public static Executable in(InputStream is) {
        return new ExpressionExecutable(readExpression(is));
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        return writeExpression(os, expression);
    }
}
