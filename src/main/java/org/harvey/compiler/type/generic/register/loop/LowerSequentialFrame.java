package org.harvey.compiler.type.generic.register.loop;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.type.generic.register.command.bounds.RegisterLowerBound;
import org.harvey.compiler.type.generic.register.entity.FullLinkType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 17:40
 */
public class LowerSequentialFrame implements CallableStackFrame {
    private final FullLinkType lower;

    public LowerSequentialFrame(FullLinkType lower) {
        this.lower = lower;
    }

    @Override
    public CallableStackFrame invokeRecursive(CallableStackFrame context) {
        EndTypeContextFrame typeContext = (EndTypeContextFrame) context;
        CallableStackFrame next = typeContext.sequentialEndType(lower);
        typeContext.add(new RegisterLowerBound());
        return next;
    }

}
