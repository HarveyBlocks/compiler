package org.harvey.compiler.exception.analysis;

import org.harvey.compiler.io.source.SourcePosition;

/**
 * 构建声明文件而产生的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 23:56
 */
public class StatementFileBuildException extends AnalysisException {
    public StatementFileBuildException(SourcePosition errorPosition, String message) {
        super(errorPosition, message);
    }

    public StatementFileBuildException(
            SourcePosition errorPositionBegin, SourcePosition errorPositionEnd,
            String message) {
        super(errorPositionBegin, errorPositionEnd, message);
    }
}
