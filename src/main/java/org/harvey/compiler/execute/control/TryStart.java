package org.harvey.compiler.execute.control;

import java.io.InputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-10 22:09
 */
public class TryStart extends BodyStart {
    public static final Byte CODE = 19;

    public static Executable in(InputStream is) {
        TryStart tryStart = new TryStart();
        tryStart.setBodyEnd(readInteger(is));
        return tryStart;
    }

    @Override
    public byte getCode() {
        return ElseStart.CODE;
    }
}
