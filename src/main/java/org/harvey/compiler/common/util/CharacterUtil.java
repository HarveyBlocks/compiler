package org.harvey.compiler.common.util;

import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.HashMap;
import java.util.Map;

/**
 * 对Character的工具类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 22:36
 */
public class CharacterUtil {
    public static final Map<Character, Integer> NUMBER_MAP;

    static {
        Map<Character, Integer> map = new HashMap<>();
        map.put('0', 0);
        map.put('1', 1);
        map.put('2', 2);
        map.put('3', 3);
        map.put('4', 4);
        map.put('5', 5);
        map.put('6', 6);
        map.put('7', 7);
        map.put('8', 8);
        map.put('9', 9);
        map.put('A', 10);
        map.put('B', 11);
        map.put('C', 12);
        map.put('D', 13);
        map.put('E', 14);
        map.put('F', 15);
        map.put('a', 10);
        map.put('b', 11);
        map.put('c', 12);
        map.put('d', 13);
        map.put('e', 14);
        map.put('f', 15);
        NUMBER_MAP = map;
    }

    private CharacterUtil() {
    }

    public static boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    public static boolean isLetter(char c) {
        return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
    }

    public static boolean isScientificNotationSign(char c) {
        return c == SourceFileConstant.SCIENTIFIC_NOTATION_UPPER_SIGN ||
               c == SourceFileConstant.SCIENTIFIC_NOTATION_LOWER_SIGN;
    }

    public static boolean notNumber(char n, int radix) {
        return !isNumber(n, radix);
    }

    public static boolean isNumber(char n, int radix) {
        if (radix > 16) {
            throw new CompilerException("Inappropriate design: radix>16");
        }
        // 先用Map实现一下, 还可以if-else实现, 太麻烦了
        if (!NUMBER_MAP.containsKey(n)) {
            return false;
        }
        return NUMBER_MAP.get(n) < radix;
    }

    public static int literalNumber(char n) {
        Integer i = NUMBER_MAP.get(n);
        return i == null ? -1 : i;
    }
}
