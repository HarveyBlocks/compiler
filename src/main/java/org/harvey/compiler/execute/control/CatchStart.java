package org.harvey.compiler.execute.control;

import lombok.Getter;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.IgnoreIdentifierString;
import org.harvey.compiler.execute.expression.SourceVariableDeclare;

import java.util.ArrayList;

/**
 * TODO  
 *
 * @date 2025-01-10 22:10
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class CatchStart extends BodyStart {
    private final IdentifierString exceptionIdentifier;
    private final IgnoreIdentifierString ignoreIdentifier;
    private final ArrayList<SourceVariableDeclare.LocalType> exceptionTypes;

    public CatchStart(ArrayList<SourceVariableDeclare.LocalType> exceptionTypes,
                      IdentifierString exceptionIdentifier,
                      IgnoreIdentifierString ignoreIdentifier) {
        this.exceptionTypes = exceptionTypes;
        this.exceptionIdentifier = exceptionIdentifier;
        this.ignoreIdentifier = ignoreIdentifier;
        if (this.exceptionTypes == null) {
            throw new CompilerException("exception types can not be null");
        } else if (this.exceptionIdentifier == null && this.ignoreIdentifier == null) {
            throw new CompilerException("exception identifier and ignore identifier can not be null at the same time");
        } else if (this.exceptionIdentifier != null && this.ignoreIdentifier != null) {
            throw new CompilerException(
                    "exception identifier and ignore identifier can not be nonnull at the same time");
        }
    }

    public boolean isIgnore() {
        return ignoreIdentifier != null;
    }

}
