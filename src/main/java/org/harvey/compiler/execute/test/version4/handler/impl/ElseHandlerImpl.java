package org.harvey.compiler.execute.test.version4.handler.impl;


import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version4.command.SimpleGotoCommand;
import org.harvey.compiler.execute.test.version4.handler.ElseHandler;
import org.harvey.compiler.execute.test.version4.msg.ControlContext;
import org.harvey.compiler.execute.test.version4.msg.Label;
import org.harvey.compiler.execute.test.version4.stack.AfterControlSentence;
import org.harvey.compiler.execute.test.version4.stack.key.ControlKey;

/**
 * 源:
 * <pre>{@code
 * else block
 * }</pre>
 * 目标:
 * [0] L: goto L'
 * [2] block
 * [3] L':
 * <p>
 * <p>
 * 思考
 * if()
 * if()
 * else if()
 * else if()
 * else if()
 * else
 * sentence;
 * sentence;
 * else
 * <p>
 * body 后面 有 body
 * or body 后面 有 sentence
 * or sentence 后面 有 sentence
 * or sentence 后面 有 body
 * 都算结束了, 把没有用括号括起来的 else if 全部清空
 * 有一个body start, 就加一个stack
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:00
 */
@SuppressWarnings("DuplicatedCode")
public class ElseHandlerImpl implements ElseHandler {
    @Override
    public void handle(ControlContext context) {
        AfterControlSentence sentence = context.bodyStack.sentenceAfterControl();
        if (!sentence.opening() || sentence.emptyOpenWith()) {
            throw new AnalysisControlException(context.now(), "except pre if");
        }
        ControlKey controlKey = sentence.popPreOpenWith();
        if (controlKey.confirm(Keyword.IF)) {
            Label skipOtherElse = context.createLabel();
            context.addSequential(new SimpleGotoCommand(skipOtherElse));
            ControlKey elseKey = new ControlKey(Keyword.ELSE);
            elseKey.setSkipExpressionAfter(skipOtherElse);
            Label ifOnFalse = controlKey.getIfOnFalse();
            context.registerLabelOnNextSequential(ifOnFalse);
            controlKey.setIfOnFalse(null);
            sentence.openWith(elseKey);
        } else {
            throw new AnalysisControlException(context.now(), "pre must expression or block end");
        }
    }

}
