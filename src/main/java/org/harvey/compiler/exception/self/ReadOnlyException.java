package org.harvey.compiler.exception.self;

/**
 * TODO  ReadOnly的异常
 *
 * @date 2025-04-04 20:40
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class ReadOnlyException extends UnsupportedOperationException {
    public ReadOnlyException() {
        super();
    }

    public ReadOnlyException(String message) {
        super("read only: "+message);
    }

    public ReadOnlyException(String message, Throwable cause) {
        super("read only: "+message, cause);
    }

    public ReadOnlyException(Throwable cause) {
        super(cause);
    }

    protected ReadOnlyException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super("read only: "+ message, cause, enableSuppression, writableStackTrace);
    }
}
