package org.harvey.compiler.io.serializer;

import lombok.Getter;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.exception.CompilerException;

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
        long maximum = Serializes.maxValue(bitCount);
        if (value > maximum) {
            throw new CompilerException("value:" + value + " can not larger than bit count maximum: " + maximum,
                    new IllegalArgumentException());
        }
        this.value = value;
        this.bitCount = bitCount;
    }

    public static void assertSupport(int bitCount) {
        if (bitCount > 64) {
            throw new CompilerException("Do not support this large value: 2^" + bitCount + " as head",
                    new IllegalArgumentException());
        }
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
