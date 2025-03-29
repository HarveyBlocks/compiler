package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.Expression;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-09 00:34
 */
@Getter
@AllArgsConstructor
public class Case extends Executable {
    public static final Byte CODE = 3;
    private final Expression constantCase;

    public static Executable in(InputStream is) {
        return new Case(readExpression(is));
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        return writeExpression(os, constantCase);
    }
}
