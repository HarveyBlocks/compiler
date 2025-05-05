package org.harvey.compiler.execute.test.version5.handler.impl;


import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.execute.test.version5.command.IfConditionGoto;
import org.harvey.compiler.execute.test.version5.handler.IfHandler;
import org.harvey.compiler.execute.test.version5.msg.ControlContext;
import org.harvey.compiler.execute.test.version5.msg.Label;
import org.harvey.compiler.execute.test.version5.msg.stack.AfterControlSentence;
import org.harvey.compiler.execute.test.version5.msg.stack.ControlKey;

/**
 * 源:
 * if(exp)block
 * 目标:
 * [0] exp
 * [1] if_eq_goto L
 * [2] block
 * [3] L:
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:00
 */
@SuppressWarnings("DuplicatedCode")
public class IfHandlerImpl implements IfHandler {
    @Override
    public void handle(ControlContext context) {
        // 1. 获取一个boolean condition expression
        // 2. 获取一个
        context.conditionExpressionHandler().handle(context);
        Label onFalse = context.createLabel();
        context.addSequential(IfConditionGoto.onFalse(onFalse));
        // after
        context.bodyStackNotEmpty();
        AfterControlSentence sentence = context.bodyStack.sentenceAfterControl();
        if (!sentence.opening()) {
            sentence.openSentence();
        }
        ControlKey ifKey = new ControlKey(Keyword.IF);
        ifKey.setIfOnFalse(onFalse);
        sentence.openWith(ifKey);
    }
}
