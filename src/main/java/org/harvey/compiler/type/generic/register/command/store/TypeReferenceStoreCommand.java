package org.harvey.compiler.type.generic.register.command.store;

import lombok.Getter;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;

/**
 * TODO
 * type的fullname被抽象成reference
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-27 21:54
 */
@Getter
public class TypeReferenceStoreCommand implements TypeStoreCommand, AtSequentialTypeCommand {
    private final ReferenceElement element;

    public TypeReferenceStoreCommand(ReferenceElement element) {
        this.element = element;
    }

    @Override
    public SourcePosition getPosition() {
        return element.getPosition();
    }

    @Override
    public String toString() {
        return "st_reference " + element.getReference();
    }
}
