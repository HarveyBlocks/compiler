package org.harvey.compiler.type.generic.register.command.store;

import lombok.Getter;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.syntax.BasicTypeString;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-27 16:45
 */
@Getter
public class BasicTypeStoreCommand implements TypeStoreCommand, AtSequentialTypeCommand {
    private final BasicTypeString basicType;

    public BasicTypeStoreCommand(BasicTypeString basicType) {
        this.basicType = basicType;
    }

    @Override
    public String toString() {
        return "store_basic_type " + basicType;
    }

    @Override
    public SourcePosition getPosition() {
        return basicType.getPosition();
    }
}
