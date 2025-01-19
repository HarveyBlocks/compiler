package org.harvey.compiler.exception.analysis;

import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-26 23:21
 */
public class FunctionBuildException extends AnalysisException {
    public FunctionBuildException(SourcePosition errorPosition, String message) {
        super(errorPosition, message);
    }

    public FunctionBuildException(SourcePosition errorPositionBegin, SourcePosition errorPositionEnd, String message) {
        super(errorPositionBegin, errorPositionEnd, message);
    }
}
