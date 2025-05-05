package org.harvey.compiler.execute.test.version2.handler;

import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version2.msg.ControlContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 19:59
 */
public interface ExecutableControlHandler {
    static void handleNext(ControlContext context, ExecutableControlHandler handler) {
        context.nowHandler(handler);
        handler.handle(context);
    }

    void handle(ControlContext context);

    default void nextIsWhile(ControlContext context) {
        handleNext(context, context.whileHandler());
    }

    default void nextIsDo(ControlContext context) {
        handleNext(context, context.doHandler());
    }

    default void nextIsIf(ControlContext context) {
        context.ifHandler().handle(context);
    }

    default void nextIsElse(ControlContext context) {
        handleNext(context, context.elseHandler());
    }

    default void notHaveNext(ControlContext context) {
        handleNext(context, context.noneNextHandler());
    }

    default void nextIsExpression(ControlContext context) {
        handleNext(context, context.sentenceExpressionHandler());
    }

    default void nextIsBlockStart(ControlContext context) {
        handleNext(context, context.bodyStartHandler());
    }

    default void nextIsDeclareType(ControlContext context) {
        handleNext(context, context.declareHandler());
    }

    default void nextIsBreakSentence(ControlContext context) {
        handleNext(context, context.breakHandler());
    }

    default void nextIsContinueSentence(ControlContext context) {
        handleNext(context, context.continueHandler());
    }


    default void nextIsBodyEnd(ControlContext controlContext) {
        throw new AnalysisControlException(controlContext.now(), "except {");
    }
}

