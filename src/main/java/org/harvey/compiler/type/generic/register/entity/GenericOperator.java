package org.harvey.compiler.type.generic.register.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.type.generic.register.command.GenericTypeRegisterCommand;
import org.harvey.compiler.type.generic.register.command.bounds.MultipleUpperBounds;
import org.harvey.compiler.type.generic.register.command.bounds.RegisterLowerBound;
import org.harvey.compiler.type.generic.register.command.bounds.RegisterUpperBound;

import java.util.function.Supplier;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 15:35
 */
@AllArgsConstructor
@Getter
public enum GenericOperator {
    PRE(10, null),
    POST(10, null),
    AND(20, MultipleUpperBounds::new),
    EXTENDS(30, RegisterUpperBound::new),
    SUPER(30, RegisterLowerBound::new),
    COMMA(40, null);
    private final int priority;
    private final Supplier<GenericTypeRegisterCommand> commandSupplier;


}
