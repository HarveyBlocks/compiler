package org.harvey.compiler.exception.execution;

import org.harvey.compiler.exception.VieCompilerException;

/**
 * CompileExecution的异常, 列入配置错误
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-27 17:08
 */
public class CompileExecutionException extends VieCompilerException {
    public CompileExecutionException() {
        super();
    }

    public CompileExecutionException(String message) {
        super(message);
    }

    public CompileExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompileExecutionException(Throwable cause) {
        super(cause);
    }

    protected CompileExecutionException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
