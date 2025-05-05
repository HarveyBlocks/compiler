package org.harvey.compiler.type.generic.register.loop;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.common.recusive.ForCallableStackFrame;
import org.harvey.compiler.exception.self.UnknownTypeException;
import org.harvey.compiler.type.generic.register.command.RegisterGenericParamCount;
import org.harvey.compiler.type.generic.register.entity.BoundsType;
import org.harvey.compiler.type.generic.register.entity.CanParameterType;
import org.harvey.compiler.type.generic.register.entity.EndType;
import org.harvey.compiler.type.generic.register.entity.FullLinkType;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 17:40
 */
public class ParamListLoopSequentialFrame implements ForCallableStackFrame {
    private final List<CanParameterType> params;
    private final FullLinkType nextType;
    private int index;
    private boolean initialed;

    public ParamListLoopSequentialFrame(List<CanParameterType> params, FullLinkType nextType) {
        this.params = params;
        this.nextType = nextType;
        this.index = 0;
        this.initialed = false;
    }

    @Override
    public void initial() {
        index = 0;
        initialed = true;
    }

    @Override
    public boolean initialed() {
        return initialed;
    }

    @Override
    public void nextStep() {
        index++;
    }


    @Override
    public boolean condition() {
        return index < params.size();
    }


    @Override
    public CallableStackFrame invokeRecursive(CallableStackFrame context) {
        CanParameterType param = params.get(index);
        EndTypeContextFrame typeContext = (EndTypeContextFrame) context;
        if (param instanceof EndType) {
            return typeContext.sequentialEndType((EndType) param);
        } else if (param instanceof BoundsType) {
            // bounds type
            return typeContext.sequentialBoundsType((BoundsType) param);
        } else {
            throw new UnknownTypeException(CanParameterType.class, param);
        }

    }

    @Override
    public CallableStackFrame invokeRecursiveAfterLoop(CallableStackFrame context) {
        if (!params.isEmpty()) {
            RegisterGenericParamCount registerGenericParamCount = new RegisterGenericParamCount(params.size());
            ((EndTypeContextFrame) context).add(registerGenericParamCount);
        }
        if (nextType == null) {
            return null;
        } else {
            return EndTypeContextFrame.newNextSequentialFrame(nextType);
        }
    }
}
