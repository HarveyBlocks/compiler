package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-10 14:21
 */
@Getter
@AllArgsConstructor
public class Continue extends Executable {
    public static final Byte CODE = 5;
    private final int continuableStart;

    public static Executable in(InputStream is) {
        return new Break(readInteger(is));
    }

    @Override
    public byte getCode() {
        return ElseStart.CODE;
    }

    @Override
    public int out(OutputStream os) {
        return writeInteger(os, continuableStart);
    }
}
