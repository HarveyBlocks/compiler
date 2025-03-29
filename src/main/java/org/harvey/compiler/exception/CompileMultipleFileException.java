package org.harvey.compiler.exception;

import org.harvey.compiler.io.source.SourcePosition;

import java.io.File;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-18 21:21
 */
public class CompileMultipleFileException extends CompileException {
    private final String originMessage;


    public CompileMultipleFileException(File file, SourcePosition errorPosition, String message) {
        super(file.getAbsolutePath() + " " + errorPosition + " : " + message);
        this.originMessage = message;
    }

    public CompileMultipleFileException(
            File file,
            SourcePosition errorPositionBegin,
            SourcePosition errorPositionEnd,
            String message) {
        super(file.getAbsolutePath() + " " + errorPositionBegin + "-" + errorPositionEnd + " : " + message);
        this.originMessage = message;
    }

    public CompileMultipleFileException(File file, SourcePosition errorPosition, String message, Throwable throwable) {
        super(errorPosition + " : " + message, throwable);
        this.originMessage = message;
    }

    public CompileMultipleFileException(
            File file,
            SourcePosition errorPositionBegin, SourcePosition errorPositionEnd, String message,
            Throwable throwable) {
        super(file.getAbsolutePath() + " " + errorPositionBegin + "-" + errorPositionEnd + " : " + message, throwable);
        this.originMessage = message;
    }
}
