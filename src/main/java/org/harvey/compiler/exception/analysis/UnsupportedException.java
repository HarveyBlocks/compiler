package org.harvey.compiler.exception.analysis;

import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-24 19:22
 */
public class UnsupportedException extends AnalysisException {
    public UnsupportedException(SourcePosition errorPosition, String unsupportedOperation) {
        super(errorPosition, message(unsupportedOperation));
    }

    public UnsupportedException(SourcePosition errorPositionBegin, SourcePosition errorPositionEnd,
                                String unsupportedOperation) {
        super(errorPositionBegin, errorPositionEnd, message(unsupportedOperation));
    }

    private static String message(String unsupportedOperation) {
        return "I'm so sorry about that " + unsupportedOperation + " has not supported at this version";
    }
}
