package org.harvey.compiler.common;

import org.harvey.compiler.exception.self.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-25 19:59
 */
public class OnlySetOnce {
    private OnlySetOnce() {
    }

    public static void settable(Object field) {
        if (field != null) {
            throw new CompilerException("filed can not update", new UnsupportedOperationException());
        }
    }

    public static void legalArgument(Object argument) {
        if (argument == null) {
            throw new CompilerException("filed can not be null", new UnsupportedOperationException());
        }
    }

}
