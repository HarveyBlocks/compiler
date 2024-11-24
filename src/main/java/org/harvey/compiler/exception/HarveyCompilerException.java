package org.harvey.compiler.exception;

/**
 * TODO  HarveyCompiler的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 19:46
 */
public class HarveyCompilerException extends RuntimeException {

    public HarveyCompilerException(String message) {
        super(message);
    }

    public HarveyCompilerException(String message, Throwable cause) {
        super(message, cause);
    }

    public HarveyCompilerException(Throwable cause) {
        super(cause);
    }

    public HarveyCompilerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public HarveyCompilerException() {
    }
}
