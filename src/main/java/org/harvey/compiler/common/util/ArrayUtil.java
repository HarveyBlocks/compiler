package org.harvey.compiler.common.util;

import org.harvey.compiler.exception.self.CompilerException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 有关数组的工具类封装
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
        return at(array, target) >= 0;
    }

    public static <E> int at(E[] array, E target) {
        if (array == null) {
            return -1;
        }
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], target)) {
                return i;
            }
        }
        return -1;
    }

    public static int sum(int[] arr) {
        int sum = 0;
        for (int e : arr) {
            sum += e;
        }
        return sum;
    }

    /**
     * @param start 包含
     * @param end   不包含
     */
    public static <T> T[] sub(T[] src, int start, int end, T[] target) {
        if (target.length != start - end) {
            throw new CompilerException(
                    "Error target length: target.length(" + target.length + ") should equals start(" + start +
                    ")-end(" + end + ")", new IllegalArgumentException());
        }
        if (end < start) {
            throw new CompilerException("end should not less than start", new IllegalArgumentException());
        }
        System.arraycopy(src, start, target, 0, end - start);
        return target;
    }

    public static <P, R> R[] map(P[] src, Function<P, R> mapper, IntFunction<R[]> generator) {
        return Arrays.stream(src).map(mapper).toArray(generator);
    }

    public static <T> List<T> toList(T[] src) {
        return Arrays.stream(src).collect(Collectors.toList());
    }

    /**
     * @return end = Math.min(array1.length, array2.length) for all same
     */
    public static <T> int firstDifferenceIndex(T[] array1, T[] array2) {
        int end = Math.min(array1.length, array2.length);
        for (int i = 0; i < end; i++) {
            if (!Objects.equals(array1[i], array2[i])) {
                return i;
            }
        }
        return end;
    }

    /**
     * from front to back
     *
     * @param catcher true for catching
     * @return -1 for catcher not catch
     */
    public static <T> int indexOf(T[] arr, Predicate<T> catcher) {
        return indexOf(arr, catcher, 0);
    }

    /**
     * from front to back
     *
     * @param fromIndex including
     * @param catcher   true for catching
     * @return -1 for catcher not catch
     */
    public static <T> int indexOf(T[] arr, Predicate<T> catcher, int fromIndex) {
        for (int i = fromIndex; i < arr.length; i++) {
            if (catcher.test(arr[i])) {
                return i;
            }
        }
        return -1;
    }

    public static <T> boolean allNotNull(T[] array) {
        if (array == null) {
            return false;
        }
        return Arrays.stream(array).allMatch(Objects::nonNull);
    }
}
