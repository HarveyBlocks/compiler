package org.harvey.compiler.type.generic.register;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.common.recusive.RecursiveInvokerRegister;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;
import org.harvey.compiler.type.generic.register.entity.ReferManager;
import org.harvey.compiler.type.generic.register.loop.DefaultTypeContextFrame;
import org.harvey.compiler.type.generic.register.loop.EndTypeContextFrame;
import org.harvey.compiler.type.generic.register.command.GenericTypeRegisterCommand;
import org.harvey.compiler.type.generic.register.entity.EndType;

import java.util.List;
import java.util.ListIterator;

/**
 * source
 * -> 逆波兰表达式({@link SimpleTypeCommandFactory})
 * ->serialize out
 * ->serialize in
 * ->referred type tree{@link SimpleTypeCommandPhaser}
 * ->referred command{@link DefaultTypeContextFrame}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-05 15:11
 */
public class RegisterTypeBuildUtil {
    public static EndType buildEndType(List<GenericTypeRegisterCommand> genericTypeRegisterCommands, ReferManager referManager) {
        if (genericTypeRegisterCommands.isEmpty()) {
            return null;
        }
        return SimpleTypeCommandPhaser.buildTree(genericTypeRegisterCommands,referManager);
    }

    /**
     * 一个文件 一个referManager
     */
    public static List<AtSequentialTypeCommand> buildSequentialCommand(EndTypeContextFrame frame) {
        RecursiveInvokerRegister register = new RecursiveInvokerRegister(() -> frame);
        CallableStackFrame context = register.execute();
        return ((EndTypeContextFrame) context).getResult();
    }
}
