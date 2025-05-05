package org.harvey.compiler.type.generic.register.command.store;

import lombok.Getter;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;

/**
 * TODO
 * fullname
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-27 15:32
 */
public class SourceTypeStoreCommand implements TypeStoreCommand, AtSequentialTypeCommand {

    private final FullIdentifierString pre;
    @Getter
    private final FullIdentifierString sourceType;

    public SourceTypeStoreCommand(FullIdentifierString pre, FullIdentifierString sourceType) {
        this.pre = pre;
        this.sourceType = sourceType;
    }

    public FullIdentifierString getPre() {
        return pre.empty() ? null : pre;
    }

    @Override
    public String toString() {
        return "store_type " + sourceType + (pre.empty() ? "" : "  (pre=" + pre + ")");
    }

    @Override
    public SourcePosition getPosition() {
        return sourceType.getPosition();
    }
}
