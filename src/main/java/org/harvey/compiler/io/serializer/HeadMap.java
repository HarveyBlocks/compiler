package org.harvey.compiler.io.serializer;

import lombok.Getter;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.exception.io.CompilerFileWriteException;
import org.harvey.compiler.exception.self.CompilerException;

/**
 * 文件头
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 22:37
 */
@Getter
public class HeadMap {

    // 值, 不超过(1<<bitCount)-1
    private final long rawValue;
    // 希望在Header中占据的bit数量
    private final int bitCount;

    public HeadMap(long rawValue, int bitCount) {
        if (bitCount <= 0) {
            throw new CompilerException("bitCount should larger than zero", new IllegalArgumentException());
        }
        assertSupport(bitCount);
        this.rawValue = rawValue;
        this.bitCount = bitCount;
    }

    public static void assertSupport(int bitCount) {
        if (bitCount >= 65 || bitCount <= 0) {
            throw new CompilerException(
                    "Do not support this large value: 2^" + bitCount + " as head",
                    new IllegalArgumentException()
            );
        }
    }

    public long getUnsignedValue() {
        return rawValue;
    }

    /**
     * {@link #bitCount} 1 表示最低位(最右)是符号, 64表示最高位(最左)是符号
     */
    public long getSignedValue() {
        int signedBit = this.bitCount - 1;
        boolean minus = (rawValue & (1L << signedBit)) != 0;
        return minus ? rawValue | fillOneBefore(signedBit) : rawValue;
    }

    /**
     * 0 -> 1111_1110
     * 1 -> 1111_1100
     * 2 -> 1111_1000
     */
    private long fillOneBefore(int signedBit) {
        long result = -2L;
        long mask = 1L;
        for (int i = 0; i < signedBit; i++) {
            mask <<= 1;
            result &= ~mask;
        }
        return result;
    }

    public HeadMap inRange(boolean unsigned, String name) {
        if (unsigned && (bitCount >= 64 || bitCount <= 0)) {
            throw new CompilerException("not support at bit increase of " + bitCount + " while the data is unsigned");
        } else if (!unsigned && (bitCount >= 65 || bitCount <= 1)) {
            throw new CompilerException("not support at bit increase of " + bitCount + " while the data is unsigned");
        }
        long maximum = unsigned ? Serializes.unsignedMaxValue(bitCount) : Serializes.signedMaxValue(bitCount);
        long rawValue = unsigned && this.rawValue < 0 ? this.rawValue + (1L << bitCount) : this.rawValue;
        if (rawValue > maximum) {
            throw new CompilerFileWriteException(
                    "the value of " + name + ", witch is" + rawValue + ", can not larger than maximum: " + maximum,
                    new IllegalArgumentException()
            );
        }
        long minim = unsigned ? 0 : Serializes.signedMinValue(bitCount);
        if (rawValue < minim) {
            throw new CompilerFileWriteException(
                    "the value of " + name + ", witch is" + rawValue + ", can not smaller than minim: " + minim,
                    new IllegalArgumentException()
            );
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
            sb.append(Serializes.getBit(rawValue, i).value());
        }
        return sb.toString();
    }
}
