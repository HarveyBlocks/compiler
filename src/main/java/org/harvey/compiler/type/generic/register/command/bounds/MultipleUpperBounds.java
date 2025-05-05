package org.harvey.compiler.type.generic.register.command.bounds;

import org.harvey.compiler.type.generic.register.command.GenericTypeRegisterCommand;

/**
 * TODO
 * extends Upper&Upper
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-28 21:36
 */
public class MultipleUpperBounds implements GenericTypeRegisterCommand {
    @Override
    public String toString() {
        return "&";
    }
}
