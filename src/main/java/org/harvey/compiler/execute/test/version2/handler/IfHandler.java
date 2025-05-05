package org.harvey.compiler.execute.test.version2.handler;

import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version2.msg.ControlContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:02
 */
public interface IfHandler extends ExecutableControlHandler {
    @Override
    default void nextIsDeclareType(ControlContext context) {
        throw new AnalysisControlException(context.now(), "not expect declare in if");
    }

    @Override
    default void nextIsElse(ControlContext context) {
        throw new AnalysisControlException(context.now(), "not expect else in if");
    }

    @Override
    default void notHaveNext(ControlContext context) {
        throw new AnalysisControlException(context.now(), "expect more after if");
    }

    @Override
    default void nextIsIf(ControlContext context) {
        ExecutableControlHandler.super.nextIsIf(context);
    }
}
