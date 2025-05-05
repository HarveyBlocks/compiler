package org.harvey.compiler.execute.test.version5.handler;

import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version5.msg.ControlContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:02
 */
@SuppressWarnings("DuplicatedCode")
public interface OneControlSentenceHandler extends ExecutableControlHandler {
    @Override
    default void nextIsWhile(ControlContext context) {
        throw new AnalysisControlException(context.now(), "except ;");
    }

    @Override
    default void nextIsDo(ControlContext context) {
        throw new AnalysisControlException(context.now(), "except ;");
    }

    @Override
    default void nextIsIf(ControlContext context) {
        throw new AnalysisControlException(context.now(), "except ;");
    }

    @Override
    default void nextIsElse(ControlContext context) {
        throw new AnalysisControlException(context.now(), "except ;");
    }

    @Override
    default void notHaveNext(ControlContext context) {
        throw new AnalysisControlException(context.now(), "except ;");
    }

    @Override
    default void nextIsExpression(ControlContext context) {
        throw new AnalysisControlException(context.now(), "except ;");
    }

    @Override
    default void nextIsBlockStart(ControlContext context) {
        throw new AnalysisControlException(context.now(), "except ;");
    }

    @Override
    default void nextIsDeclareType(ControlContext context) {
        throw new AnalysisControlException(context.now(), "except ;");
    }

    @Override
    default void nextIsBreakSentence(ControlContext context) {
        throw new AnalysisControlException(context.now(), "except ;");
    }

    @Override
    default void nextIsContinueSentence(ControlContext context) {
        throw new AnalysisControlException(context.now(), "except ;");
    }

}
