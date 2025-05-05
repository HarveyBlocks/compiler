package org.harvey.compiler.execute.test.version4.handler.impl;


import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.execute.test.version4.handler.DoHandler;
import org.harvey.compiler.execute.test.version4.msg.ControlContext;
import org.harvey.compiler.execute.test.version4.msg.Label;
import org.harvey.compiler.execute.test.version4.stack.AfterControlSentence;
import org.harvey.compiler.execute.test.version4.stack.key.ControlKey;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 00:18
 */
public class DoHandlerImpl implements DoHandler {

    @Override
    public void handle(ControlContext context) {
        Label returnBackDo = context.createLabel();
        context.registerLabelOnNextSequential(returnBackDo);
        AfterControlSentence sentence = context.bodyStack.sentenceAfterControl();
        ControlKey controlKey = new ControlKey(Keyword.DO);
        controlKey.setSentenceEndAfterControl(false);
        controlKey.setActiveReturnBack(returnBackDo);
        Label skipDo = context.createLabel();
        controlKey.setSkipExpressionAfter(skipDo);
        sentence.openWith(controlKey);
        context.pushBreakAndContinue(skipDo, returnBackDo);
    }
}
