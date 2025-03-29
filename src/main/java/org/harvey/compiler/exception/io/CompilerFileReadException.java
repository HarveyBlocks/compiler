package org.harvey.compiler.exception.io;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 22:23
 */
public class CompilerFileReadException extends CompilerFileIOException {
    public CompilerFileReadException(String message) {
        super(message);
    }

    public CompilerFileReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilerFileReadException(Throwable cause) {
        super(cause);
    }

    public CompilerFileReadException(
            String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CompilerFileReadException() {
    }
}
