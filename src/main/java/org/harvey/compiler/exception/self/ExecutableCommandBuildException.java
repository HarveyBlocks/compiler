package org.harvey.compiler.exception.self;

/**
 * TODO  IllegalCommand的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-05 15:25
 */
public class ExecutableCommandBuildException extends CompilerException {
    public ExecutableCommandBuildException() {
        super();
    }

    public ExecutableCommandBuildException(String message) {
        super(message);
    }

    public ExecutableCommandBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutableCommandBuildException(Throwable cause) {
        super(cause);
    }

    protected ExecutableCommandBuildException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
