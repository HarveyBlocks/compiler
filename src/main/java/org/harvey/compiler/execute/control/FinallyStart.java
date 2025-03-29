package org.harvey.compiler.execute.control;

import java.io.InputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-10 22:09
 */
public class FinallyStart extends BodyStart {
    public static final Byte CODE = 12;

    public static Executable in(InputStream is) {
        FinallyStart finallyStart = new FinallyStart();
        finallyStart.setBodyEnd(readInteger(is));
        return finallyStart;
    }

    @Override
    public byte getCode() {
        return ElseStart.CODE;
    }
}
