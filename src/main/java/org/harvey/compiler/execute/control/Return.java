package org.harvey.compiler.execute.control;

import lombok.Getter;
import org.harvey.compiler.execute.expression.Expression;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 返回的内容是由上一条Executable决定, 如果是return;, 那么在Executable中是 [EMPTY, return]
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-10 20:24
 */
@Getter
public class Return extends SequentialExecutable {
    public static final Byte CODE = 16;
    private final Expression returnExpression;

    public Return(Expression returnExpression) {
        this.returnExpression = returnExpression;
    }

    public Return() {
        this(Expression.EMPTY);
    }

    public static Executable in(InputStream is) {
        return new Return(readExpression(is));
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        return writeExpression(os, returnExpression);
    }
}
