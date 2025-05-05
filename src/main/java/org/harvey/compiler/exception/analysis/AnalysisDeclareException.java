package org.harvey.compiler.exception.analysis;

import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO  AnalysisDeclare的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-05 15:32
 */
public class AnalysisDeclareException extends AnalysisException {
    public AnalysisDeclareException(SourcePosition errorPosition, String message) {
        super(errorPosition, message);
    }

    public AnalysisDeclareException(
            SourcePosition errorPositionBegin, SourcePosition errorPositionEnd,
            String message) {
        super(errorPositionBegin, errorPositionEnd, message);
    }
}
