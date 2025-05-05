package org.harvey.compiler.type.generic.register.command;

import lombok.Getter;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-27 15:32
 */
@Getter
public class RegisterGenericParamCount implements AtSequentialTypeCommand {
    private final int parameterCount;

    public RegisterGenericParamCount(int parameterCount) {
        this.parameterCount = parameterCount;
    }

    @Override
    public String toString() {
        return "register_generic " + parameterCount;
    }
}
