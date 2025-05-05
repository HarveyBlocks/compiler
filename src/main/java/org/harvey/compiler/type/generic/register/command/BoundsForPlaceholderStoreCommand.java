package org.harvey.compiler.type.generic.register.command;

import lombok.Getter;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;
import org.harvey.compiler.type.generic.register.command.store.TypeStoreCommand;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-28 21:43
 */
@Getter
public class BoundsForPlaceholderStoreCommand implements TypeStoreCommand, AtSequentialTypeCommand {
    private final SourcePosition position;

    public BoundsForPlaceholderStoreCommand(SourcePosition position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "store_bounds";
    }
}
