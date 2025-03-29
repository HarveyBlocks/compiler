package org.harvey.compiler.exception.io;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 02:13
 */
public class CompilerFileWriteException extends CompilerFileIOException {
    public CompilerFileWriteException(String message) {
        super(message);
    }

    public CompilerFileWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilerFileWriteException(Throwable cause) {
        super(cause);
    }

    public CompilerFileWriteException(
            String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CompilerFileWriteException() {
        super();
    }
}
