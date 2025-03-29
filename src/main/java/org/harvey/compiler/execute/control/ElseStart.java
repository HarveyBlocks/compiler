package org.harvey.compiler.execute.control;

import lombok.Setter;

import java.io.InputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 23:33
 */
@Setter
public class ElseStart extends BodyStart {
    public static final Byte CODE = 10;

    public static Executable in(InputStream is) {
        ElseStart elseStart = new ElseStart();
        elseStart.setBodyEnd(readInteger(is));
        return elseStart;
    }

    @Override
    public byte getCode() {
        return ElseStart.CODE;
    }
}
