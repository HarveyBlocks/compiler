package org.harvey.compiler.exception.io;

import lombok.Getter;
import org.harvey.compiler.exception.CompileException;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 编译而产生的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 23:55
 */
@Getter
public class CompileFileException extends CompileException {
    private final String originMessage;


    public CompileFileException(SourcePosition errorPosition, String message) {
        super(errorPosition + " : " + message);
        this.originMessage = message;
    }

    public CompileFileException(SourcePosition errorPositionBegin, SourcePosition errorPositionEnd, String message) {
        super(errorPositionBegin + "-" + errorPositionEnd + " : " + message);
        this.originMessage = message;
    }

    public CompileFileException(SourcePosition errorPosition, String message, Throwable throwable) {
        super(errorPosition + " : " + message, throwable);
        this.originMessage = message;
    }

    public CompileFileException(
            SourcePosition errorPositionBegin, SourcePosition errorPositionEnd, String message,
            Throwable throwable) {
        super(errorPositionBegin + "-" + errorPositionEnd + " : " + message, throwable);
        this.originMessage = message;
    }
}
