package org.harvey.compiler.common.util;

import org.harvey.compiler.exception.self.CompilerException;

/**
 * 大端
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-05 16:00
 */
public class ByteUtil {
    private static final long MAX_RIGHT_1 = Long.MIN_VALUE >>> 1;

    public static byte[] toRawBytes(byte value) {
        return new byte[]{value};
    }

    public static byte[] toRawBytes(short value) {
        return new byte[]{(byte) (value >>> 8), (byte) (value << 8 >>> 8)};
    }

    public static byte[] toRawBytes(int value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value << 8 >>> 24), (byte) (value << 16 >>> 24), (byte) (
                value << 24 >>> 24)};
    }

    public static byte[] toRawBytes(long value) {
        return new byte[]{(byte) (value >>> 56), (byte) (value << 8 >>> 56), (byte) (value << 16 >>> 56), (byte) (
                value << 24 >>> 56), (byte) (value << 32 >>> 56), (byte) (value << 40 >>> 56), (byte) (value << 48 >>>
                                                                                                       56), (byte) (
                value << 56 >>>
                56),};
    }

    public static long phaseRawBytes8(byte[] bytes) {
        if (bytes.length != 8) {
            throw new CompilerException("bytes length should be 8", new IllegalArgumentException());
        }
        long result = 0;
        for (byte data : bytes) {
            result <<= 8;
            result |= (int) (data) & 0xff;
        }
        return result;
    }

    public static int phaseRawBytes4(byte[] bytes) {
        if (bytes.length != 4) {
            throw new CompilerException("bytes length should be 4", new IllegalArgumentException());
        }
        int result = 0;
        for (byte data : bytes) {
            result <<= 8;
            result |= ((int) (data)) & 0xff;
        }
        return result;
    }

    public static long phaseRawBytes(byte[] bytes) {
        boolean minus = bytes.length > 0 && bytes[0] < 0;
        byte[] bytes8 = new byte[8];
        for (int i = 0; i < 8 - bytes.length; i++) {
            bytes8[i] = (byte) (minus ? 0xff : 0);
        }
        System.arraycopy(bytes, 0, bytes8, 8 - bytes.length, bytes.length);
        return phaseRawBytes8(bytes8);
    }

    public static byte[] phaseUnsignedInt4(String result, int radix) {
        byte[] bytes = phaseUnsignedInt8(result, radix);
        for (int i = 0; i < 4; i++) {
            if (bytes[i] != 0) {
                throw new NumberFormatException("Overflow");
            }
        }
        byte[] uint4 = new byte[4];
        System.arraycopy(bytes, 4, uint4, 0, 4);
        return uint4;
    }

    public static byte[] phaseUnsignedInt8(String number, int radix) {
        if (radix < 2 || radix > 16) {
            throw new CompilerException("Bad radix");
        }
        long limit = Long.MAX_VALUE;
        long multiplyMin = limit / radix + 1;
        long uint8 = 0;
        int i = 0;
        int len = number.length();
        while (i < len) {
            int digit = CharacterUtil.literalNumber(number.charAt(i++));
            if (digit < 0) {
                throw new NumberFormatException("Illegal digit");
            }
            if (digit >= radix) {
                throw new NumberFormatException("Illegal digit collectionIn radix " + radix);
            }

            if (uint8 >= multiplyMin) {
                uint8 = Long.MIN_VALUE + digit - ((MAX_RIGHT_1 / radix << 1) - uint8 / radix * radix) * radix +
                        (MAX_RIGHT_1 % radix << 1) + uint8 % radix * radix;
                if (i < len) {
                    throw new NumberFormatException("Overflow");
                } else {
                    break;
                }
            }
            uint8 *= radix;
            if (uint8 > limit - digit) {
                uint8 = Long.MIN_VALUE + digit - (Long.MAX_VALUE - uint8 + 1); // 1000_0000
                if (i < len) {
                    throw new NumberFormatException("Overflow");
                } else {
                    break;
                }
            }
            uint8 += digit;
        }
        return toRawBytes(uint8);
    }


}
