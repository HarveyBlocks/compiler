package org.harvey.compiler.execute.expression;

import org.harvey.compiler.common.CompileProperties;
import org.harvey.compiler.common.SourceFileConstant;
import org.harvey.compiler.common.util.CharacterUtil;
import org.harvey.compiler.common.util.ListPoint;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * TODO
 *
 * @date 2025-01-08 16:51
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class LiterallyConstantUtil {

    private static final EscapeMatcher[] MATCHERS = new EscapeMatcher[]{
            new Basic(), new Octal(), new Utf16()
    };

    public static byte[] stringData(String value) {
        return textData(value, SourceFileConstant.STRING_ENCIRCLE_SIGN)
                .getBytes(CompileProperties.SOURCE_FILE_CHARSET);
    }

    public static byte[] charData(String value) {
        String result = textData(value, SourceFileConstant.CHARACTER_ENCIRCLE_SIGN);
        if (result.length() != 1) {
            throw new AnalysisExpressionException(SourcePosition.UNKNOWN, "Too many characters in");
        }
        return result.getBytes(CompileProperties.SOURCE_FILE_CHARSET);
    }

    private static String textData(String value, char encircle) {
        if (value.length() < 2 ||
                value.charAt(0) != encircle ||
                value.charAt(value.length() - 1) != encircle) {
            throw new CompilerException("Illegal call, char should encircle with `" +
                    encircle + "` ", new IllegalArgumentException());
        }
        return dealEscape(value.substring(1, value.length() - 1));
    }

    private static String dealEscape(String src) {
        StringBuilder sb = new StringBuilder();
        char[] charArray = src.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            Character next = charArray[i];
            if (next != SourceFileConstant.ESCAPE_CHARACTER_IDENTIFIERS) {
                sb.append(next);
                continue;
            }
            ListPoint<Character> escape = null;
            if (i == charArray.length - 1) {
                throw new AnalysisExpressionException(SourcePosition.UNKNOWN, "Incomplete escape string");
            }
            // 直到找全了
            for (EscapeMatcher matcher : MATCHERS) {
                escape = matcher.apply(src, i);
                if (escape == null) {
                    break;
                }
            }
            if (escape == null) {
                // 没用能被解析的方法
                throw new AnalysisExpressionException(SourcePosition.UNKNOWN, "Unknown escape string");
            }
            sb.append(escape.getElement());
            i = escape.getIndex();
        }
        return sb.toString();
    }

    private interface EscapeMatcher extends BiFunction<String, Integer, ListPoint<Character>> {
    }

    private static class Basic implements EscapeMatcher {
        public static final Map<Character, Character> ESCAPE_MAP =
                SourceFileConstant.ESCAPE_CHARACTER_MAP;

        @Override
        public ListPoint<Character> apply(String es, Integer si) {
            int index = si;
            if (es.length() <= index) {
                return null;
            }
            return new ListPoint<>(index + 1, ESCAPE_MAP.get(es.charAt(index)));
        }
    }

    private static class Octal implements EscapeMatcher {
        @Override
        public ListPoint<Character> apply(String es, Integer si) {
            int index = si;
            if (es.length() <= index + 2) {
                return null;
            }
            // +八进制数字
            char c1 = es.charAt(index++);
            char c2 = es.charAt(index++);
            char c3 = es.charAt(index++);
            if (CharacterUtil.notNumber(c1, 8) ||
                    CharacterUtil.notNumber(c2, 8) ||
                    CharacterUtil.notNumber(c3, 8)) {
                return null;
            }
            int n1 = CharacterUtil.literalNumber(c1);
            int n2 = CharacterUtil.literalNumber(c2);
            int n3 = CharacterUtil.literalNumber(c3);
            int codePoint = (n1 << 6) & (n2 << 3) & n3;
            char[] chars = Character.toChars(codePoint);
            if (chars.length != 1) {
                return null;
            }
            return new ListPoint<>(index, chars[0]);
        }
    }

    private static class Utf16 implements EscapeMatcher {
        @Override
        public ListPoint<Character> apply(String es, Integer si) {
            // start index, \的后一个
            int index = si;
            // si+0 -> u
            // si+1 ->
            if (es.length() <= index + 4) {
                return null;
            }
            if (es.charAt(index++) != 'u') {
                return null;
            }
            // 获取三个数字
            // utf-16
            char c1 = es.charAt(index++);
            char c2 = es.charAt(index++);
            char c3 = es.charAt(index++);
            char c4 = es.charAt(index++);
            if (CharacterUtil.notNumber(c1, 16) ||
                    CharacterUtil.notNumber(c2, 16) ||
                    CharacterUtil.notNumber(c3, 16) ||
                    CharacterUtil.notNumber(c4, 16)) {
                return null;
            }
            int n1 = CharacterUtil.literalNumber(c1);
            int n2 = CharacterUtil.literalNumber(c2);
            int n3 = CharacterUtil.literalNumber(c3);
            int n4 = CharacterUtil.literalNumber(c4);
            int codePoint = (n1 << 24) & (n2 << 16) & (n3 << 8) & n4;
            char[] chars = Character.toChars(codePoint);
            if (chars.length != 1) {
                return null;
            }
            return new ListPoint<>(index, chars[0]);
        }
    }


}
