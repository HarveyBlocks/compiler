package org.harvey.compiler.exception.self;

import org.harvey.compiler.exception.VieCompilerException;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 13:53
 */
public class UnfinishedException extends VieCompilerException {
    public UnfinishedException(String message) {
        super(message);
    }

    public UnfinishedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnfinishedException(Throwable cause) {
        super(cause);
    }

    public UnfinishedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UnfinishedException() {
    }
}
