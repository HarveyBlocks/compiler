package org.harvey.compiler.io.serializer;

import lombok.Getter;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.io.CompilerFileWriterException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 22:37
 */
@Getter
public class HeadMap {

    // 值, 不超过(1<<bitCount)-1
    private final long value;
    // 希望在Header中占据的bit数量
    private final int bitCount;

    public HeadMap(long value, int bitCount) {
        if (bitCount <= 0) {
            throw new CompilerException("bitCount should larger than zero",
                    new IllegalArgumentException());
        }
        assertSupport(bitCount);
        this.value = value;
        this.bitCount = bitCount;
    }

    public static void assertSupport(int bitCount) {
        if (bitCount >= 65 || bitCount <= 0) {
            throw new CompilerException("Do not support this large value: 2^" + bitCount + " as head",
                    new IllegalArgumentException());
        }
    }

    public HeadMap inRange(boolean unsigned, String name) {
        if (unsigned && (bitCount >= 64 || bitCount <= 0)) {
            throw new CompilerException("not support at bit count of " + bitCount + " while the data is unsigned");
        } else if (!unsigned && (bitCount >= 65 || bitCount <= 1)) {
            throw new CompilerException("not support at bit count of " + bitCount + " while the data is unsigned");
        }
        long maximum = unsigned ? Serializes.unsignedMaxValue(bitCount) : Serializes.signedMaxValue(bitCount);
        long rawValue = unsigned && value < 0 ? value + (1L << bitCount) : value;
        if (rawValue > maximum) {
            throw new CompilerFileWriterException(
                    "the value of " + name + ", witch is" + rawValue +
                            ", can not larger than maximum: " + maximum,
                    new IllegalArgumentException());
        }
        long minim = unsigned ? 0 : Serializes.signedMinValue(bitCount);
        if (rawValue < minim) {
            throw new CompilerFileWriterException(
                    "the value of " + name + ", witch is" + rawValue +
                            ", can not smaller than minim: " + minim,
                    new IllegalArgumentException());
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("0x");
        for (int i = -bitCount; i < 0; i++) {
            if (i % 4 == 0) {
                sb.append("_");
            }
            sb.append(Serializes.getBit(value, i).value());
        }
        return sb.toString();
    }
}
