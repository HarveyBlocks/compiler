package org.harvey.compiler.execute.control;

import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 23:35
 */
@Getter
@Setter
public class BodyStart extends Executable {
    public static final int NOT_CONFIRMED_REFERENCE = -1;
    public static final Byte CODE = 1;
    private int bodyEnd = NOT_CONFIRMED_REFERENCE;

    protected BodyStart(int bodyEnd) {
        this.bodyEnd = bodyEnd;
    }

    public BodyStart() {
    }

    public static Executable in(InputStream is) {
        int bodyEnd = readInteger(is);
        return new BodyStart(bodyEnd);
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        return writeInteger(os, bodyEnd);
    }
}
