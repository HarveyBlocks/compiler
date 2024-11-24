package org.harvey.compiler.common;

import java.util.Map;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-15 22:01
 */
public class PropertyConstant {
    public static final String SINGLE_LINE_COMMENTS_START = "//";
    public static final String SINGLE_LINE_COMMENTS_END = SystemConstant.LINE_SEPARATOR;
    public static final String MULTI_LINE_COMMENTS_PRE = "/*";
    public static final String MULTI_LINE_COMMENTS_POST = "*/";
    public static final char ESCAPE_CHARACTER_IDENTIFIERS = '\\';
    public static final char STRING_ENCIRCLE_SIGN = '"';

    public static final char CHARACTER_ENCIRCLE_SIGN = '\'';
    public static final char SENTENCE_END = ';';

    public static final char BODY_START = '{';
    public static final char BODY_END = '}';
    public static final Set<Character> READABLE_WHITESPACE = Set.of(' ');
    public static final Set<Character> SPRITE_SIGN = Set.of(BODY_START, BODY_END, SENTENCE_END);
    public static final Map<Character, Character> ESCAPE_CHARACTER_MAP = Map.of(
            'r', '\r',
            'n', '\n',
            't', '\t',
            'b', '\b',
            'f', '\f',
            '\\', '\\',
            '\"', '\"',
            '\'', '\''
    );
    public static final char SCIENTIFIC_NOTATION_LOWER_SIGN = 'e';
    public static final char SCIENTIFIC_NOTATION_UPPER_SIGN = 'E';
    // 用于分割数字和Word
    public static final char ITEM_SEPARATE_SIGN = '_';
    public static final char DOT = '.';
    public static final char DECIMAL_POINT = DOT;
    public static final char GET_MEMBER = DOT;

}
