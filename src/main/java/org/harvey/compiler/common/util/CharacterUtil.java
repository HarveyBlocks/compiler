package org.harvey.compiler.common.util;

import org.harvey.compiler.common.PropertyConstant;
import org.harvey.compiler.exception.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 22:36
 */
public class CharacterUtil {
    private CharacterUtil() {
    }

    public static boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    public static boolean isLetter(char c) {
        return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
    }

    public static boolean isScientificNotationSign(char c) {
        return c == PropertyConstant.SCIENTIFIC_NOTATION_UPPER_SIGN || c == PropertyConstant.SCIENTIFIC_NOTATION_LOWER_SIGN;
    }

    /**
     * @param origin 要求以`/`开头
     */
    public static String escapeCharacter(String origin) {
        // TODO
        return null;
    }
}
