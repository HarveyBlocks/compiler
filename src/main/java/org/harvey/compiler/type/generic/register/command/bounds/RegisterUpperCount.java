package org.harvey.compiler.type.generic.register.command.bounds;

import lombok.Getter;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-29 22:18
 */
@Getter
public class RegisterUpperCount implements AtSequentialTypeCommand {
    private final int upperBoundCount;

    public RegisterUpperCount(int upperBoundCount) {
        this.upperBoundCount = upperBoundCount;
    }

    @Override
    public String toString() {
        return "register_upper_bound " + upperBoundCount;
    }
}