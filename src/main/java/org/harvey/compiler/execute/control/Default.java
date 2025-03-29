package org.harvey.compiler.execute.control;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-09 00:36
 */
public class Default extends Executable {
    public static final Byte CODE = 7;

    public static Executable in(InputStream is) {
        return new Default();
    }

    @Override
    public byte getCode() {
        return ElseStart.CODE;
    }

    @Override
    public int out(OutputStream os) {
        return 0;
    }
}
