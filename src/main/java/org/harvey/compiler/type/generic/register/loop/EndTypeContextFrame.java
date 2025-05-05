package org.harvey.compiler.type.generic.register.loop;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.syntax.BasicTypeString;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;
import org.harvey.compiler.type.generic.register.entity.BoundsType;
import org.harvey.compiler.type.generic.register.entity.CanParameterType;
import org.harvey.compiler.type.generic.register.entity.EndType;
import org.harvey.compiler.type.generic.register.entity.FullLinkType;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 17:40
 */
public abstract class EndTypeContextFrame implements CallableStackFrame {


    static CallableStackFrame newParamListLoopSequentialFrame(
            List<CanParameterType> paramList, FullLinkType nextType) {
        return new ParamListLoopSequentialFrame(paramList, nextType);
    }

    public static CallableStackFrame newNextSequentialFrame(FullLinkType nextType) {
        return new NextSequentialFrame(nextType);
    }

    public static CallableStackFrame newUpperListLoopSequentialFrame(
            List<FullLinkType> upper, FullLinkType lower) {
        return new UpperListLoopSequentialFrame(upper, lower);
    }

    public static CallableStackFrame newLowerSequentialFrame(
            FullLinkType lower) {
        return new LowerSequentialFrame(lower);
    }

    public abstract List<AtSequentialTypeCommand> getResult();

    public abstract void add(AtSequentialTypeCommand element);


    public CallableStackFrame sequentialEndType(EndType type) {
        return new EndTypeSequentialBuilder(type).invokeRecursive(this);
    }

    public CallableStackFrame sequentialBoundsType(BoundsType type) {
        return new BoundsTypeSequentialBuilder(type).invokeRecursive(this);
    }
}
