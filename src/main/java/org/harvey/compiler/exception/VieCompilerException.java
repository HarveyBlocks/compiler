package org.harvey.compiler.exception;

/**
 *   VieCompiler的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 19:46
 */
public class VieCompilerException extends RuntimeException {

    public VieCompilerException(String message) {
        super(message);
    }

    public VieCompilerException(String message, Throwable cause) {
        super(message, cause);
    }

    public VieCompilerException(Throwable cause) {
        super(cause);
    }

    public VieCompilerException(
            String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public VieCompilerException() {
    }
}
