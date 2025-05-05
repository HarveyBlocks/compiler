package org.harvey.compiler.execute.test.version3.handler.impl;


import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.execute.test.version3.handler.DoHandler;
import org.harvey.compiler.execute.test.version3.msg.ControlContext;
import org.harvey.compiler.execute.test.version3.msg.Label;
import org.harvey.compiler.execute.test.version3.stack.AfterControlSentence;
import org.harvey.compiler.execute.test.version3.stack.key.ControlKey;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 00:18
 */
public class DefaultDoHandler implements DoHandler {

    @Override
    public void handle(ControlContext context) {
        Label doLabel = context.createLabel();
        context.registerLabelOnNextSequential(doLabel);
        AfterControlSentence sentence = context.bodyStack.sentenceAfterControl();
        ControlKey controlKey = new ControlKey(Keyword.DO);
        controlKey.setSentenceEndAfterControl(false);
        controlKey.setActiveReturnBack(doLabel);
        sentence.openWith(controlKey);
    }
}
