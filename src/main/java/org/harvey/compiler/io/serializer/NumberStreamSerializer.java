package org.harvey.compiler.io.serializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 用于将数字序列化, 效率低, 不建议使用
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-13 15:32
 */
public class NumberStreamSerializer implements StreamSerializer<Long> {
    private final int bitCount;
    private final boolean signed;

    public NumberStreamSerializer(int bitCount, boolean signed) {
        this.bitCount = bitCount;
        this.signed = signed;
    }

    @Override
    public Long in(InputStream is) {
        return StreamSerializerUtil.readNumber(is, bitCount, signed);
    }

    @Override
    public int out(OutputStream os, Long src) {
        return StreamSerializerUtil.writeNumber(os, src, bitCount, signed);
    }

    public static class ForInteger implements StreamSerializer<Integer> {
        private final NumberStreamSerializer numberStreamSerializer;

        public ForInteger(int bitCount, boolean signed) {
            numberStreamSerializer = new NumberStreamSerializer(bitCount, signed);
        }

        @Override
        public Integer in(InputStream is) {
            return numberStreamSerializer.in(is).intValue();
        }

        @Override
        public int out(OutputStream os, Integer src) {
            return numberStreamSerializer.out(os, Long.valueOf(src));
        }
    }
}
