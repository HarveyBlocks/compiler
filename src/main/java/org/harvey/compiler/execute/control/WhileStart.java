package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class WhileStart extends BodyStart {
    public static final Byte CODE = 21;
    private final Expression condition;

    public static Executable in(InputStream is) {
        int bodyEnd = readInteger(is);
        Expression condition = readExpression(is);
        WhileStart whileStart = new WhileStart(condition);
        whileStart.setBodyEnd(bodyEnd);
        return whileStart;
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
