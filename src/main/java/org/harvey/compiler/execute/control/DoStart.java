package org.harvey.compiler.execute.control;

import java.io.InputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 23:36
 */
public class DoStart extends BodyStart {
    public static final Byte CODE = 8;

    public static Executable in(InputStream is) {
        DoStart doStart = new DoStart();
        doStart.setBodyEnd(readInteger(is));
        return doStart;
    }

    @Override
    public byte getCode() {
        return CODE;
    }


}
