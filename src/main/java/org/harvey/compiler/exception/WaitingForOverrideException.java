package org.harvey.compiler.exception;

/**
 * TODO  WaitingForOverride的异常
 *
 * @date 2025-01-21 22:09
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class WaitingForOverrideException extends VieCompilerException {
    public WaitingForOverrideException(Object... ignore) {
        super();
    }

    public WaitingForOverrideException(String message) {
        super(message);
    }

    public WaitingForOverrideException(String message, Throwable cause) {
        super(message, cause);
    }

    public WaitingForOverrideException(Throwable cause) {
        super(cause);
    }

    protected WaitingForOverrideException(String message,
                                          Throwable cause,
                                          boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
