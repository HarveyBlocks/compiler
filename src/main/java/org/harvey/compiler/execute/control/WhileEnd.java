package org.harvey.compiler.execute.control;

import lombok.Getter;
import org.harvey.compiler.execute.expression.Expression;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 23:37
 */
@Getter
public class WhileEnd extends BodyEnd {
    public static final Byte CODE = 20;
    private final Expression condition;

    public WhileEnd(int start, Expression condition) {
        super(start);
        this.condition = condition;
    }

    public static Executable in(InputStream is) {
        int start = readInteger(is);
        Expression condition = readExpression(is);
        return new WhileEnd(start, condition);
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        return super.out(os) + writeExpression(os, condition);
    }
}
