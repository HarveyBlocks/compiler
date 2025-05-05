package org.harvey.compiler.exception.analysis;

import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-05 15:52
 */
public class AnalysisControlException extends AnalysisExpressionException{
    public AnalysisControlException(SourcePosition errorPosition, String message) {
        super(errorPosition, message);
    }

    public AnalysisControlException(SourcePosition errorPositionBegin, SourcePosition errorPositionEnd, String message) {
        super(errorPositionBegin, errorPositionEnd, message);
    }
}
