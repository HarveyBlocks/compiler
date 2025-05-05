package org.harvey.compiler.exception.io;

import org.harvey.compiler.exception.VieCompilerException;

/**
 * VieIO的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-18 20:25
 */
public class VieIOException extends VieCompilerException {
    public VieIOException() {
        super();
    }

    public VieIOException(String message) {
        super(message);
    }

    public VieIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public VieIOException(Throwable cause) {
        super(cause);
    }

    protected VieIOException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
