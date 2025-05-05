package org.harvey.compiler.exception.self;

/**
 * TODO  UnknownType的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 00:18
 */
public class UnknownTypeException extends RuntimeException {
    public UnknownTypeException() {
        super();
    }

    public <T, V extends T> UnknownTypeException(Class<T> upper, V unknownTypeObj) {
        super("Unknown type of " + upper.getSimpleName() + ":" + unknownTypeObj.getClass().getSimpleName());
    }

    public UnknownTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownTypeException(Throwable cause) {
        super(cause);
    }

    protected UnknownTypeException(
            String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
