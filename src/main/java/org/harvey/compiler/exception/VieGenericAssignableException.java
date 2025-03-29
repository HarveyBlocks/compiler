package org.harvey.compiler.exception;

import lombok.Getter;
import org.harvey.compiler.io.source.SourcePosition;

/**
 *   VieGenericAssignable的异常
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-10 19:45
 */
@Getter
public class VieGenericAssignableException extends Exception {
    private final SourcePosition attachment;

    public VieGenericAssignableException(SourcePosition attachment) {
        super();
        this.attachment = attachment;
    }

    public VieGenericAssignableException(SourcePosition attachment, String message) {
        super(message);
        this.attachment = attachment;
    }

    public VieGenericAssignableException(SourcePosition attachment, String message, Throwable cause) {
        super(message, cause);
        this.attachment = attachment;
    }

    public VieGenericAssignableException(SourcePosition attachment, Throwable cause) {
        super(cause);
        this.attachment = attachment;
    }

    protected VieGenericAssignableException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace, SourcePosition attachment) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.attachment = attachment;
    }
}
