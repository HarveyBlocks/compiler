package org.harvey.compiler.execute.test.version2.handler.impl;


import org.harvey.compiler.execute.test.version2.command.IfConditionGoto;
import org.harvey.compiler.execute.test.version2.handler.IfHandler;
import org.harvey.compiler.execute.test.version2.msg.ControlContext;
import org.harvey.compiler.execute.test.version2.msg.Label;
import org.harvey.compiler.execute.test.version2.stack.IfSentence;

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
public class DefaultIfHandler implements IfHandler {
    @Override
    public void handle(ControlContext context) {
        // 1. 获取一个boolean condition expression
        // 2. 获取一个
        context.conditionExpressionHandler().handle(context);
        Label label = context.createLabel();
        context.addSequential(IfConditionGoto.onFalse(label));
        // after
        context.bodyStackNotEmpty();
        IfSentence ifSentence = context.bodyStack.sentenceForIf();
        if (!ifSentence.starting()) {
            ifSentence.initSentence();
        }
        ifSentence.pushLastIfFalse(label);
        ifSentence.startIfSentence();
    }
}
