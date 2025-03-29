package org.harvey.compiler.exception.analysis;

import org.harvey.compiler.exception.CompileFileException;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 编译的分析过程中产生的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 00:00
 */
public class AnalysisException extends CompileFileException {
    public AnalysisException(SourcePosition errorPosition, String message) {
        super(errorPosition, message);
    }

    public AnalysisException(SourcePosition errorPositionBegin, SourcePosition errorPositionEnd, String message) {
        super(errorPositionBegin, errorPositionEnd, message);
    }
}
