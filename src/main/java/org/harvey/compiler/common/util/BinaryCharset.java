package org.harvey.compiler.common.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * 存储数值数据
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-05 13:53
 */
public class BinaryCharset extends Charset {
    public static final BinaryCharset INSTANCE = new BinaryCharset();

    private BinaryCharset() {
        super("BINARY", new String[0]);
    }

    @Override
    public boolean contains(Charset cs) {
        return false;
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new BinaryDecoder(this);
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new BinaryEncoder(this);
    }

    static class BinaryDecoder extends CharsetDecoder {
        protected BinaryDecoder(Charset cs) {
            super(cs, 1, 1);
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            while (in.hasRemaining()) {
                byte b1 = in.get();
                out.put((char) (b1));
            }
            return CoderResult.UNDERFLOW;
        }
    }

    static class BinaryEncoder extends CharsetEncoder {

        protected BinaryEncoder(Charset cs) {
            super(cs, 1, 1);
        }

        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            while (in.hasRemaining()) {
                char c = in.get();
                out.put((byte) (c & 0xff));
            }
            return CoderResult.UNDERFLOW;
        }
    }

}
