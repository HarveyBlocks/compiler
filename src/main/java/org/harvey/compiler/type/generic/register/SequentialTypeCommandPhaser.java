package org.harvey.compiler.type.generic.register;

import org.harvey.compiler.exception.self.ExecutableCommandBuildException;
import org.harvey.compiler.exception.self.UnknownTypeException;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.type.generic.register.command.BoundsForPlaceholderStoreCommand;
import org.harvey.compiler.type.generic.register.command.GenericTypeRegisterCommand;
import org.harvey.compiler.type.generic.register.command.InnerType;
import org.harvey.compiler.type.generic.register.command.RegisterGenericParamCount;
import org.harvey.compiler.type.generic.register.command.bounds.RegisterLowerBound;
import org.harvey.compiler.type.generic.register.command.bounds.RegisterUpperCount;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;
import org.harvey.compiler.type.generic.register.command.store.BasicTypeStoreCommand;
import org.harvey.compiler.type.generic.register.command.store.TypeReferenceStoreCommand;
import org.harvey.compiler.type.generic.register.entity.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-05 01:34
 */
public class SequentialTypeCommandPhaser {

    public static EndType phaseCommand(
            List<AtSequentialTypeCommand> sequentialTypeCommands) {
        Stack<CanParameterType> stack = new Stack<>();
        for (AtSequentialTypeCommand command : sequentialTypeCommands) {
            if (command instanceof TypeReferenceStoreCommand) {
                storeTypeReference((TypeReferenceStoreCommand) command, stack);
            } else if (command instanceof BasicTypeStoreCommand) {
                storeBasicType((BasicTypeStoreCommand) command, stack);
            } else if (command instanceof BoundsForPlaceholderStoreCommand) {
                storeBoundsForPlaceholder((BoundsForPlaceholderStoreCommand) command, stack);
            } else if (command instanceof RegisterGenericParamCount) {
                registerGenericParam((RegisterGenericParamCount) command, stack);
            } else if (command instanceof RegisterUpperCount) {
                registerUpperBound((RegisterUpperCount) command, stack);
            } else if (command instanceof RegisterLowerBound) {
                registerLowerBound(stack);
            } else if (command instanceof InnerType) {
                setInner(stack);
            } else {
                throw new UnknownTypeException(GenericTypeRegisterCommand.class, command);
            }

        }
        if (stack.size() != 1) {
            throw new ExecutableCommandBuildException("In the end there is more than one type");
        }
        return returnEndType(stack.pop());
    }


    private static void storeTypeReference(
            TypeReferenceStoreCommand command, Stack<CanParameterType> stack) {
        ReferenceElement element = command.getElement();
        stack.push(new FullLinkType(element));
    }

    private static void storeBasicType(
            BasicTypeStoreCommand command, Stack<CanParameterType> stack) {
        stack.push(new BasicType(command.getBasicType()));
    }

    private static void storeBoundsForPlaceholder(
            BoundsForPlaceholderStoreCommand command, Stack<CanParameterType> stack) {
        // 占位符 Placeholders
        stack.push(new BoundsType(command.getPosition()));
    }

    private static void registerGenericParam(
            RegisterGenericParamCount command, Stack<CanParameterType> stack) {
        int parameterCount = command.getParameterCount();
        LinkedList<CanParameterType> parameters = new LinkedList<>();
        while (parameterCount-- > 0) {
            CanParameterType top = stack.pop();
            if (top instanceof FullLinkType) {
                top = ((FullLinkType) top).toOutermost();
            }
            parameters.addFirst(top);
        }
        CanParameterType pop = stack.pop();
        if (!(pop instanceof FullLinkType)) {
            throw new ExecutableCommandBuildException("set parameters on " + pop.getClass().getSimpleName());
        }
        FullLinkType target = (FullLinkType) pop;
        if (!target.getParams().isEmpty()) {
            throw new ExecutableCommandBuildException("repeated set parameters");
        }
        target.addAllParam(parameters);
        stack.push(target);
    }

    private static void registerUpperBound(RegisterUpperCount command, Stack<CanParameterType> stack) {
        int upperBoundCount = command.getUpperBoundCount();
        LinkedList<FullLinkType> uppers = new LinkedList<>();
        while (upperBoundCount-- > 0) {
            CanParameterType top = stack.pop();
            if (!(top instanceof FullLinkType)) {
                throw new ExecutableCommandBuildException("set " + top.getClass().getSimpleName() + " as upper");
            }
            // 注意顺序
            uppers.addFirst(((FullLinkType) top).toOutermost());
        }
        CanParameterType pop = stack.pop();
        if (!(pop instanceof BoundsType)) {
            throw new ExecutableCommandBuildException("set upper to " + pop.getClass().getSimpleName());
        }
        BoundsType target = (BoundsType) pop;
        if (!target.getUppers().isEmpty()) {
            throw new ExecutableCommandBuildException("repeated set uppers");
        }
        target.addAllUppers(uppers);
        stack.push(target);
    }

    private static void registerLowerBound(Stack<CanParameterType> stack) {
        CanParameterType lower = stack.pop();
        CanParameterType pop = stack.pop();
        if (!(pop instanceof BoundsType)) {
            throw new ExecutableCommandBuildException("set lower to " + pop.getClass().getSimpleName());
        }
        BoundsType bound = (BoundsType) pop;
        if (!(lower instanceof FullLinkType)) {
            throw new ExecutableCommandBuildException("set " + lower.getClass().getSimpleName() + " as lower");
        }
        if (bound.getLower() != null) {
            throw new ExecutableCommandBuildException("has set lower");
        }
        bound.setLower(((FullLinkType) lower).toOutermost());
        stack.push(bound);
    }

    private static void setInner(Stack<CanParameterType> stack) {
        CanParameterType inner = stack.pop();
        CanParameterType pop = stack.pop();
        if (!(pop instanceof FullLinkType)) {
            throw new ExecutableCommandBuildException("set inner to " + pop.getClass().getSimpleName());
        }
        FullLinkType outer = (FullLinkType) pop;
        if (!(inner instanceof FullLinkType)) {
            throw new ExecutableCommandBuildException("set " + pop.getClass().getSimpleName() + "as inner");
        }

        // 要么另外存一份要么用pre
        if (!outer.innermost()) {
            throw new ExecutableCommandBuildException("outer has set inner");
        }
        FullLinkType innerType = (FullLinkType) inner;
        FullLinkType outermostOfInner = innerType.toOutermost();
        outer.setInnerType(outermostOfInner);
        outermostOfInner.setOuterType(outer);
        // 比较好的是push(inner)而不是push outer
        stack.push(inner);
    }

    private static EndType returnEndType(CanParameterType result) {
        if (result instanceof FullLinkType) {
            return ((FullLinkType) result).toOutermost();
        } else if (result instanceof BasicType) {
            return (BasicType) result;
        } else {
            throw new ExecutableCommandBuildException("make " + result.getClass().getSimpleName() + " as result");
        }
    }
}
