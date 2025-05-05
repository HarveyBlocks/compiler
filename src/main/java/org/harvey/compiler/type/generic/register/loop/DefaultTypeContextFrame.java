package org.harvey.compiler.type.generic.register.loop;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.syntax.BasicTypeString;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;
import org.harvey.compiler.type.generic.register.entity.EndType;
import org.harvey.compiler.type.generic.register.entity.ReferManager;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 17:40
 */
public class DefaultTypeContextFrame extends EndTypeContextFrame {
    private final List<AtSequentialTypeCommand> result;
    private final EndTypeSequentialBuilder recursivePortals;


    public DefaultTypeContextFrame(
            EndType type) {
        this.result = new LinkedList<>();
        this.recursivePortals = new EndTypeSequentialBuilder(type);
    }

    @Override
    public CallableStackFrame invokeRecursive(CallableStackFrame context) {
        return recursivePortals.invokeRecursive(context);
    }

    @Override
    public List<AtSequentialTypeCommand> getResult() {
        return result;
    }


    @Override
    public void add(AtSequentialTypeCommand element) {
        // MainTest.registerDepth();
        result.add(element);
    }


}
