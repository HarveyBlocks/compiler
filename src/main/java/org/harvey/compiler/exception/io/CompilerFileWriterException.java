package org.harvey.compiler.exception.io;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 02:13
 */
public class CompilerFileWriterException extends CompilerFileIOException {
    public CompilerFileWriterException(String message) {
        super(message);
    }

    public CompilerFileWriterException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilerFileWriterException(Throwable cause) {
        super(cause);
    }

    public CompilerFileWriterException(String message, Throwable cause, boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CompilerFileWriterException() {
        super();
    }
}
