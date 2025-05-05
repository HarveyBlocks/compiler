package org.harvey.compiler.exception.analysis;

import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-05 15:45
 */
public class AnalysisTypeException extends AnalysisExecutableException {
    public AnalysisTypeException(SourcePosition errorPosition, String message) {
        super(errorPosition, message);
    }

    public AnalysisTypeException(
            SourcePosition errorPositionBegin, SourcePosition errorPositionEnd,
            String message) {
        super(errorPositionBegin, errorPositionEnd, message);
    }
}
