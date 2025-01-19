package org.harvey.compiler.exception.io;

import org.harvey.compiler.exception.VieCompilerException;

/**
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-30 16:01
 */
public class CompilerFileIOException extends VieCompilerException {
    public CompilerFileIOException(String message) {
        super(message);
    }

    public CompilerFileIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilerFileIOException(Throwable cause) {
        super(cause);
    }

    public CompilerFileIOException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CompilerFileIOException() {
    }
}
