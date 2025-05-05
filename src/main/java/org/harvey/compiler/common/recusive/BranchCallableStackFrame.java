package org.harvey.compiler.common.recusive;

/**
 * 分支
 */
public interface BranchCallableStackFrame extends CallableStackFrame {
    boolean condition();

    default CallableStackFrame invokeRecursiveOnElse(CallableStackFrame context) {
        return null;
    }
}
