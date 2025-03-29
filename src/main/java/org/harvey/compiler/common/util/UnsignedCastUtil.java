package org.harvey.compiler.common.util;

/**
 * 类型转换, 转换时高位用'0'补全高位
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-27 22:55
 */
public class UnsignedCastUtil {
    private UnsignedCastUtil() {
    }

    public static long cast(int raw) {
        return 0xffffffffL & ((long) raw);
    }

    public static long cast(short raw) {
        return 0xffffL & ((long) raw);
    }

    public static long cast(byte raw) {
        return 0xffL & ((long) raw);
    }

    public static int toInt(long raw) {
        return (int) raw;
    }

    public static short toShort(long raw) {
        return (short) raw;
    }

    public static byte toByte(long raw) {
        return (byte) raw;
    }
}
