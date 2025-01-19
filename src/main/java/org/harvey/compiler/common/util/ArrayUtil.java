package org.harvey.compiler.common.util;

import java.util.Objects;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-16 14:58
 */
public class ArrayUtil {
    private ArrayUtil() {
    }

    /**
     * @see Objects#equals(Object, Object)
     * @see Object#equals(Object)
     */
    public static <E> boolean contains(E[] array, E target) {
        if (array == null) {
            return false;
        }
        for (E e : array) {
            if (Objects.equals(e, target)) {
                return true;
            }
        }
        return false;
    }

    public static int sum(int[] arr) {
        int sum = 0;
        for (int e : arr) {
            sum += e;
        }
        return sum;
    }
}
