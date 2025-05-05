package org.harvey.compiler.common.recusive;

import java.util.Stack;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-30 12:53
 */
public class RecursiveInvokerRegister {
    private final FrameInitializer initializer;

    public RecursiveInvokerRegister(FrameInitializer initializer) {
        this.initializer = initializer;
    }

    public CallableStackFrame execute() {
        CallableStackFrame context = initializer.instanceInitFrame();
        Stack<CallableStackFrame> stack = new Stack<>();
        stack.push(context);
        while (!stack.empty()) {
            CallableStackFrame top = stack.pop();
            if (top instanceof BranchCallableStackFrame) {
                BranchCallableStackFrame frame = (BranchCallableStackFrame) top;
                CallableStackFrame next;
                if (frame.condition()) {
                    next = frame.invokeRecursiveOnElse(context);
                } else {
                    next = frame.invokeRecursive(context);
                }
                if (next == null) {
                    continue;
                }
                stack.push(next);
            } else if (top instanceof ForCallableStackFrame) {
                ForCallableStackFrame frame = (ForCallableStackFrame) top;
                if (!frame.initialed()) {
                    frame.initial();
                }
                CallableStackFrame next;
                if (frame.condition()) {
                    next = frame.invokeRecursive(context);
                    frame.nextStep();
                    stack.push(frame);
                } else {
                    next = frame.invokeRecursiveAfterLoop(context);
                }
                if (next == null) {
                    continue;
                }
                stack.push(next);
            } else {
                CallableStackFrame next = top.invokeRecursive(context);
                if (next == null) {
                    continue;
                }
                stack.push(next);
            }
        }
        return context;
    }


}
