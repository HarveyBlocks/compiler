package org.harvey.compiler.execute.test.version3.handler.impl;

import org.harvey.compiler.execute.test.version3.handler.BodyEndHandler;
import org.harvey.compiler.execute.test.version3.msg.ControlContext;
import org.harvey.compiler.execute.test.version3.stack.AfterControlSentence;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 21:42
 */
@SuppressWarnings("DuplicatedCode")
public class DefaultBodyEndHandler implements BodyEndHandler {
    @Override
    public void handle(ControlContext context) {
        AfterControlSentence inBodySentence = context.bodyStack.sentenceAfterControl();
        if (inBodySentence.opening()) {
            SentenceEndExpressionHandler.dealLoggingLabels(context, inBodySentence);
            inBodySentence.closeSentence();
        }
        context.bodyStack.bodyOut();
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
        SentenceEndExpressionHandler.beforeWhileForDo(context);
        BodyEndHandler.super.nextIsElse(context);
    }

}
