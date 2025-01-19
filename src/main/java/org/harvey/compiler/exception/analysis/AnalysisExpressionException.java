package org.harvey.compiler.exception.analysis;

import org.harvey.compiler.io.source.SourcePosition;

/**
 * 分析表达式产生的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 23:57
 */
public class AnalysisExpressionException extends AnalysisException {

    public AnalysisExpressionException(SourcePosition errorPosition, String message) {
        super(errorPosition, message);
    }

    public AnalysisExpressionException(SourcePosition errorPositionBegin, SourcePosition errorPositionEnd,
                                       String message) {
        super(errorPositionBegin, errorPositionEnd, message);
    }
}
