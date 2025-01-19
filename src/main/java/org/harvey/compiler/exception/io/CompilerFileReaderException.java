package org.harvey.compiler.exception.io;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 22:23
 */
public class CompilerFileReaderException extends CompilerFileIOException {
    public CompilerFileReaderException(String message) {
        super(message);
    }

    public CompilerFileReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilerFileReaderException(Throwable cause) {
        super(cause);
    }

    public CompilerFileReaderException(String message, Throwable cause, boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CompilerFileReaderException() {
    }
}
