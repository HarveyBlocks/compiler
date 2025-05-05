package org.harvey.compiler.execute.test.version5.handler.impl;

import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version5.command.IfConditionGoto;
import org.harvey.compiler.execute.test.version5.handler.WhileHandler;
import org.harvey.compiler.execute.test.version5.msg.ControlContext;
import org.harvey.compiler.execute.test.version5.msg.Label;
import org.harvey.compiler.execute.test.version5.msg.stack.AfterControlSentence;
import org.harvey.compiler.execute.test.version5.msg.stack.ControlKey;
import org.harvey.compiler.io.source.SourceString;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-09 20:29
 */
public class WhileHandlerImpl implements WhileHandler {
    @Override
    public void handle(ControlContext context) {
        AfterControlSentence sentence = context.bodyStack.sentenceAfterControl();
        if (!sentence.confirmTop(Keyword.DO)) {
            handleNormalWhile(context);
            return;
        }
        ControlKey doKey = sentence.popPreOpenWith();
        if (doKey.isSentenceEndAfterControl()) {
            // do 后面一定要有一个表达式才行
            handleDoWhile(context, doKey);
        } else {
            sentence.openWith(doKey);
            handleNormalWhile(context);
        }
    }

    private void handleDoWhile(ControlContext context, ControlKey doKey) {
        // 0. while 以过
        // 1. 过condition
        context.conditionExpressionHandler().handle(context);
        // 2. 过sentence end
        SourceString next = context.next();
        if (!";".equals(next.getValue())) {
            throw new AnalysisControlException(next.getPosition(), "excepted ;");
        }
        context.previousMove(); // 保留分号, 让后面的表达式处理器去处理expression的结束
        // 3.
        Label returnBack = doKey.getActiveReturnBack();
        context.addSequential(IfConditionGoto.onTrue(returnBack));
        // for break
        Label skipExpressionAfter = doKey.getSkipExpressionAfter();
        context.registerLabelOnNextSequential(skipExpressionAfter);
        context.popContinueAndBreak();
    }

    private void handleNormalWhile(ControlContext context) {
        // while的逻辑
        Label returnBack = context.createLabel();
        context.registerLabelOnNextSequential(returnBack);
        context.conditionExpressionHandler().handle(context);
        Label onFalse = context.createLabel();
        context.addSequential(IfConditionGoto.onFalse(onFalse));
        // after
        context.bodyStackNotEmpty();
        AfterControlSentence sentence = context.bodyStack.sentenceAfterControl();
        if (!sentence.opening()) {
            sentence.openSentence();
        }
        ControlKey controlKey = new ControlKey(Keyword.WHILE);
        controlKey.setSkipExpressionAfter(onFalse);
        controlKey.setPassiveReturnBack(returnBack);
        sentence.openWith(controlKey);
    }
}
