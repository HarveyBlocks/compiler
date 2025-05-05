package org.harvey.compiler.execute.test.version3.command;

import lombok.Getter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:08
 */
@SuppressWarnings("DuplicatedCode")
@Getter
public class DefaultSequentialCommand implements SequentialCommand {
    private final String command;

    public DefaultSequentialCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }
}

