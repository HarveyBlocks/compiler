package org.harvey.compiler.exception.analysis;

import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-24 19:25
 */
public class GenericUnsupportedException extends UnsupportedException {
    public GenericUnsupportedException(SourcePosition errorPosition) {
        super(errorPosition, "generic");
    }

    public GenericUnsupportedException(SourcePosition errorPositionBegin, SourcePosition errorPositionEnd) {
        super(errorPositionBegin, errorPositionEnd, "generic");
    }
}
