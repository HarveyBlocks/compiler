package org.harvey.compiler.type.generic.register.loop;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.type.generic.register.command.InnerType;
import org.harvey.compiler.type.generic.register.entity.FullLinkType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 17:40
 */
public class NextSequentialFrame implements CallableStackFrame {
    private final FullLinkType nextType;

    public NextSequentialFrame(FullLinkType nextType) {
        this.nextType = nextType;
    }

    @Override
    public CallableStackFrame invokeRecursive(CallableStackFrame context) {
        EndTypeContextFrame typeContext = (EndTypeContextFrame) context;
        CallableStackFrame next = typeContext.sequentialEndType(nextType);
        typeContext.add(new InnerType());
        return next;
    }

}
