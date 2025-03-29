package org.harvey.compiler.execute.control;

import lombok.Getter;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 23:29
 */
@Getter
public class BodyEnd extends Executable {

    public static final Byte CODE = 0;
    private final int start;

    public BodyEnd(int start) {
        this.start = start;
    }


    public static Executable in(InputStream is) {
        int start = readInteger(is);
        return new BodyEnd(start);
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        return writeInteger(os, start);
    }
}
