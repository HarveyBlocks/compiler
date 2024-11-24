package org.harvey.compiler.exception;

import org.harvey.compiler.common.entity.SourcePosition;

/**
 * 编译而产生的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 23:55
 */
public class CompileException extends HarveyCompilerException {
    public CompileException(SourcePosition errorPosition, String message) {
        super(errorPosition + ":" + message);
    }

    public CompileException(SourcePosition errorPositionBegin, SourcePosition errorPositionEnd, String message) {
        super(errorPositionBegin + "-" + errorPositionEnd + ":" + message);
    }
}
