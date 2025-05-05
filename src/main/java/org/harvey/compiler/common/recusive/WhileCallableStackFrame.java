package org.harvey.compiler.common.recusive;

/**
 * on while (condition){
 * }
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-30 12:52
 */
public interface WhileCallableStackFrame extends ForCallableStackFrame {
    @Override
    default void initial() {

    }

    @Override
    default void nextStep() {

    }

    @Override
    default boolean initialed() {
        return true;
    }
}
