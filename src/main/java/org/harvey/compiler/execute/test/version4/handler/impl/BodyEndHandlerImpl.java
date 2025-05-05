package org.harvey.compiler.execute.test.version4.handler.impl;

import org.harvey.compiler.execute.test.version4.handler.BodyEndHandler;
import org.harvey.compiler.execute.test.version4.msg.ControlContext;
import org.harvey.compiler.execute.test.version4.stack.AfterControlSentence;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 21:42
 */
@SuppressWarnings("DuplicatedCode")
public class BodyEndHandlerImpl implements BodyEndHandler {
    private static void bodyOut(ControlContext context) {
        if (context.bodyStack.isSwitchBody()) {
            context.bodyStack.bodyOut();
            return;
        }
        // 处理switch table
        context.switchHandler().switchTable(context, context.bodyStack.switchBody());
        context.popSwitchBreak(); // break for switch
        context.bodyStack.bodyOut();
    }

    @Override
    public void handle(ControlContext context) {
        AfterControlSentence inBodySentence = context.bodyStack.sentenceAfterControl();
        if (inBodySentence.opening()) {
            SentenceEndExpressionHandler.dealLoggingLabels(context, inBodySentence);
            inBodySentence.closeSentence();
        }
        bodyOut(context);
        if (context.bodyStack.empty()) {
            return;
        }
        AfterControlSentence outBodySentence = context.bodyStack.sentenceAfterControl();
        if (outBodySentence.stillSentence()) {
            outBodySentence.expressionEnd();
            SentenceEndExpressionHandler.markOneSentence(outBodySentence);
            SentenceEndExpressionHandler.skipExpressionAfter(context, outBodySentence);
            return;
        }
        if (outBodySentence.opening()) {
            SentenceEndExpressionHandler.dealLoggingLabels(context, outBodySentence);
            outBodySentence.closeSentence();
        }
    }

    @Override
    public void nextIsElse(ControlContext context) {
        SentenceEndExpressionHandler.beforeElse(context);
        BodyEndHandler.super.nextIsElse(context);
    }

    @Override
    public void nextIsDo(ControlContext context) {
        SentenceEndExpressionHandler.beforeWhileForDo(context);
        BodyEndHandler.super.nextIsDo(context);
    }
}
