package org.harvey.compiler.common;

import org.harvey.compiler.exception.io.CompilerFileWriterException;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 16:00
 */
public class Serializes {
    private Serializes() {
    }


    public static byte[] subNumber(long number, int bytes) {
        byte[] data = new byte[bytes];
        for (int i = data.length - 1; i >= 0; i--) {
            data[i] = (byte) number;
            number >>>= 8;
        }
        return data;
    }

    public static SerializableData makeHead(HeadMap... maps) {
        if (maps == null) {
            return null;
        }
        if (maps.length == 0) {
            return SerializableData.EMPTY;
        }
        int headBc = 0;
        for (HeadMap map : maps) {
            headBc += map.getBitCount();
        }
        SerializableData head = new SerializableData(bitCountToByteCount(headBc));
        int bitIndex = 0;
        byte[] target = head.data();
        for (HeadMap map : maps) {
            int mapBitCount = map.getBitCount();
            long mapValue = map.getValue();
            int byteStartIndex = bitIndex >>> 3;
            int bitStartIndex = bitIndex & 7;
            bitIndex += mapBitCount;
            int byteEndIndex = bitIndex >>> 3;
            int bitEndIndex = bitIndex & 7;
            if (byteStartIndex == byteEndIndex) {
                // 同一个byte里的操作
                // 将mapBitCount长度的mapValue填入bitStartIndex到bitEndIndex
                if (bitStartIndex > bitEndIndex) {
                    throw new CompilerFileWriterException("I don't known how it happens, it's imposible",
                            new IllegalStateException());
                }
                target[byteStartIndex] |= (byte) (mapValue << (8 - bitEndIndex));
                continue;
            }
            // 取出mapValue的最高位填补bitStartIndex后面不足一个byte的空缺

            int newBitStart = -mapBitCount + 8 - bitStartIndex;
            byte startBit = (byte) getBits(mapValue, -mapBitCount, newBitStart);
            target[byteStartIndex] |= startBit;
            byteStartIndex++;
            // 取出mapValue的最低位填补bitEndIndex多出的byte
            if (bitEndIndex != 0) {
                byte endBit = (byte) getBits(mapValue, -bitEndIndex, 0);
                target[byteEndIndex] |= (byte) (endBit << (8 - bitEndIndex));
            }
            byteEndIndex--;
            // byte的全部赋值
            for (int i = byteStartIndex; i <= byteEndIndex; i++) {
                target[i] = (byte) getBits(mapValue, newBitStart, newBitStart + 8);
                newBitStart += 8;
            }
        }
        return head;
    }

    public static long toNumber(byte[] data) {
        // TODO
        if (data == null || data.length == 0) {
            return 0;
        }
        if (data.length > 8) {
            throw new CompilerFileWriterException("Too large, please use BigDecimal");
        }
        long result = 0;
        for (byte datum : data) {
            result <<= 8;
            result |= datum;
        }
        return result;
    }

    public static long getBits(long value, int start, int end) {
        long result = 0;
        for (int i = start; i < end; i++) {
            result <<= 1;
            result += getBit(value, i).value();
        }
        return result;
    }

    public static Bit getBit(long value, int index) {
        if (index < 0) {
            index = 64 + index;
        }
        return Bit.get(value & (1L << 63 >>> index));
    }

    public static long unsignedByteToLong(byte b) {
        return b >= 0 ? b : 256 + b;
    }

    public static long getLow(long value, int bitCount) {
        return value & lowBitFullOneMark(bitCount);
    }

    public static long lowBitFullOneMark(int bitCount) {
        if (bitCount == 0) {
            return 0;
        }
        return unsignedMaxValue(bitCount);
    }

    public static long unsignedMaxValue(int bitCount) {
        return ((1L << bitCount - 1) - 1 << 1) | 1;
    }

    public static long signedMaxValue(int bitCount) {
        return unsignedMaxValue(bitCount - 1);
    }

    public static long signedMinValue(int bitCount) {
        return -unsignedMaxValue(bitCount-1)-1;
    }

    /**
     * 除八, 并向上取整<br>
     * 24->3
     * 23->3    16+7
     * 22->3    16+6
     * 25->4    16+9
     */
    public static int bitCountToByteCount(int bitCount) {
        return ((bitCount - 1) >>> 3) + 1;
    }

    public static void notTooLong(long value, String valueName, long limited) {
        if (value > limited) {
            throw new CompilerFileWriterException("Too long of " + valueName + ": " + value + " , max is: " + limited);
        }

    }

    public static void notTooMuch(long value, String valueName, long limited) {
        if (value > limited) {
            throw new CompilerFileWriterException("Too much of " + valueName + ": " + value + " , max is: " + limited);
        }

    }

    public enum Bit {
        ZERO, ONE;

        public static Bit get(long value) {
            return value == 0 ? ZERO : ONE;
        }

        public static Bit get(boolean value) {
            return value ? ONE : ZERO;
        }

        public static Bit get(Object value) {
            return value == null ? ZERO : ONE;
        }

        /**
         * (this == ZERO ? 0 : 1)
         */
        public byte value() {
            return (byte) (this == ZERO ? 0 : 1);
        }
    }
}
