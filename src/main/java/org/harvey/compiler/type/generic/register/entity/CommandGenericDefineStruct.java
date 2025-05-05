package org.harvey.compiler.type.generic.register.entity;

import lombok.AllArgsConstructor;
import org.harvey.compiler.declare.ParamListValidPredicate;
import org.harvey.compiler.exception.self.ExecutableCommandBuildException;
import org.harvey.compiler.exception.self.UnsupportedOperationException;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.register.command.GenericTypeRegisterCommand;
import org.harvey.compiler.type.generic.register.command.store.TypeStoreCommand;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-05 20:01
 */
@AllArgsConstructor
public class CommandGenericDefineStruct implements ParamListValidPredicate.Param {
    private final IdentifierString identifier;
    // 不被序列化
    private final SourcePosition multiple;
    private final List<? extends List<? extends GenericTypeRegisterCommand>> uppers;
    private final List<? extends GenericTypeRegisterCommand> lower;
    private final List<? extends GenericTypeRegisterCommand> defaultType;

    @Override
    public boolean isMultiple() {
        return multiple != null;
    }

    @Override
    public boolean hasDefault() {
        return !defaultType.isEmpty();
    }

    @Override
    public SourcePosition getDefaultPosition() {
        if (defaultType.isEmpty()) {
            throw new UnsupportedOperationException("it is no default");
        }
        GenericTypeRegisterCommand genericTypeRegisterCommand = defaultType.get(0);
        if (genericTypeRegisterCommand instanceof TypeStoreCommand) {
            return ((TypeStoreCommand) genericTypeRegisterCommand).getPosition();
        }
        throw new ExecutableCommandBuildException(
                "first should be store command, but: " + genericTypeRegisterCommand.getClass());
    }

    @Override
    public SourcePosition getMultiple() {
        if (multiple == null) {
            throw new UnsupportedOperationException("it is no multiple");
        }
        return multiple;
    }
}
