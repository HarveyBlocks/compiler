package org.harvey.compiler.common.util;

import org.harvey.compiler.common.StatementConstant;
import org.harvey.compiler.exception.CompilerException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:52
 */
public class StringUtil {
    private StringUtil() {
    }

    public static String[] emptyStringArray() {
        return new String[]{};
    }

    public static String emptyString() {
        return "";
    }

    /**
     * 分割
     *
     * @param source    "A.B.C"->{"A","B","C"}; ".B."->{"","B",""};
     * @param separator 分隔符
     * @return 分割后的数组, 其中参数中有是null, 则返回null
     */
    public static String[] split(String source, String separator) {
        if (source == null || separator == null) {
            return null;
        }
        if (source.isEmpty()) {
            return emptyStringArray();
        }
        if (separator.isEmpty()) {
            throw new CompilerException("separator should not be empty");
        }
        int index = 0;
        int fromIndex = 0;
        List<String> result = new ArrayList<>();
        while (index < source.length()) {
            index = source.indexOf(separator, fromIndex);
            if (index < 0) {
                index = source.length();
            }
            result.add(source.substring(fromIndex, index));
            fromIndex = index + separator.length();
        }
        return result.toArray(emptyStringArray());
    }

    public static boolean endsWithIgnoreCase(String src, String end) {
        return src.toUpperCase().endsWith(end.toUpperCase());
    }

    public static boolean startsWithIgnoreCase(String src, String end) {
        return src.toUpperCase().startsWith(end.toUpperCase());
    }

    public static boolean endWithNumberSuffix(String s) {
        char lastAt = Character.toUpperCase(s.charAt(s.length() - 1));
        return lastAt == StatementConstant.FLOAT32_SUFFIX || lastAt == StatementConstant.INT64_SUFFIX;
    }

    public static boolean contains(String src, char target) {
        return src.indexOf(target) >= 0;
    }

    public static boolean containsIgnoreCase(String src, char target) {
        String upperSrc = src.toUpperCase();
        char upperTarget = Character.toUpperCase(target);
        return contains(upperSrc, upperTarget);
    }

    public static String substring(String src, int end) {
        return StringUtil.substring(src, 0, end);
    }

    public static String substring(String src, int begin, int end) {
        if (end < 0) {
            end = src.length() + end;
        }
        return src.substring(begin, end);
    }

    public static String concat(Collection<String> errorMessage) {
        StringBuilder sb = new StringBuilder();
        errorMessage.forEach(sb::append);
        return sb.toString();
    }

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
