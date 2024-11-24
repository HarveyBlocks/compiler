package org.harvey.compiler.exception.analysis;

import org.harvey.compiler.common.entity.SourcePosition;

/**
 * 构建声明语句产生的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 23:58
 */
public class StatementExpressionBuildExceptionException extends AnalysisException {
    public StatementExpressionBuildExceptionException(SourcePosition errorPosition, String message) {
        super(errorPosition, message);
    }

    public StatementExpressionBuildExceptionException(SourcePosition errorPositionBegin, SourcePosition errorPositionEnd, String message) {
        super(errorPositionBegin, errorPositionEnd, message);
    }
}
