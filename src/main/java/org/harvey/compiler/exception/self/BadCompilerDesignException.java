package org.harvey.compiler.exception.self;

/**
 * TODO  BadCompilerDesign的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-01 23:44
 */
public class BadCompilerDesignException extends RuntimeException {
    public BadCompilerDesignException() {
        super();
    }

    public BadCompilerDesignException(String message) {
        super(message);
    }

    public BadCompilerDesignException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadCompilerDesignException(Throwable cause) {
        super(cause);
    }

    protected BadCompilerDesignException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
