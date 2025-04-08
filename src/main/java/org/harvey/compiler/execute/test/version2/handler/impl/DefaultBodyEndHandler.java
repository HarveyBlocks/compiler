package org.harvey.compiler.execute.test.version2.handler.impl;

import org.harvey.compiler.execute.test.version2.handler.BodyEndHandler;
import org.harvey.compiler.execute.test.version2.msg.ControlContext;
import org.harvey.compiler.execute.test.version2.stack.IfSentence;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 21:42
 */
public class DefaultBodyEndHandler implements BodyEndHandler {
    @Override
    public void handle(ControlContext context) {
        context.bodyStack.bodyOut();
        if (context.bodyStack.empty()) {
            return;
        }
        IfSentence ifSentence = context.bodyStack.sentenceForIf();
        if (ifSentence.stillSentence()) {
            SentenceEndExpressionHandler.postExpression(context, ifSentence);
        } else {
            if (ifSentence.starting()) {
                SentenceEndExpressionHandler.sentenceFinished(context, ifSentence);
            }
        }
    }
}
