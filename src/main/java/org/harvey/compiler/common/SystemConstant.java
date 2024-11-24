package org.harvey.compiler.common;

import java.io.File;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-15 21:45
 */
public final class SystemConstant {

    public static final String LINE_SEPARATOR;

    public static final String FILE_SEPARATOR;
    public static final Set<Character> WIRED_CHARACTERS;

    static {
        LINE_SEPARATOR = System.lineSeparator();
        FILE_SEPARATOR = File.separator;
        WIRED_CHARACTERS = Set.of(
                ' ', '\n', '\r', '\t', '\f'
        );
    }

    private SystemConstant() {
    }

    public static boolean isLineSeparatorPart(char c) {
        return LINE_SEPARATOR.indexOf(c) != -1;
    }
}
