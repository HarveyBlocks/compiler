package org.harvey.compiler.type.generic.register.loop;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.common.recusive.ForCallableStackFrame;
import org.harvey.compiler.type.generic.register.command.bounds.RegisterUpperCount;
import org.harvey.compiler.type.generic.register.entity.FullLinkType;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 17:40
 */
public class UpperListLoopSequentialFrame implements ForCallableStackFrame {
    private final List<FullLinkType> uppers;
    private final FullLinkType lower;
    private int index;
    private boolean initialed;

    public UpperListLoopSequentialFrame(List<FullLinkType> uppers, FullLinkType lower) {
        this.uppers = uppers;
        this.lower = lower;
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
        return index < uppers.size();
    }


    @Override
    public CallableStackFrame invokeRecursive(CallableStackFrame context) {
        return ((EndTypeContextFrame) context).sequentialEndType(uppers.get(index));
    }

    @Override
    public CallableStackFrame invokeRecursiveAfterLoop(CallableStackFrame context) {
        if (!uppers.isEmpty()) {
            RegisterUpperCount registerUpperCount = new RegisterUpperCount(uppers.size());
            ((EndTypeContextFrame) context).add(registerUpperCount);
        }
        if (lower == null) {
            return null;
        } else {
            return EndTypeContextFrame.newLowerSequentialFrame(lower);
        }
    }
}
