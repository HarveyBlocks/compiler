package org.harvey.compiler.execute.test.version2.handler.impl;


import org.harvey.compiler.execute.test.version2.handler.ElseHandler;
import org.harvey.compiler.execute.test.version2.msg.ControlContext;
import org.harvey.compiler.execute.test.version2.msg.Label;
import org.harvey.compiler.execute.test.version2.stack.IfSentence;

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
public class DefaultElseHandler implements ElseHandler {
    @Override
    public void handle(ControlContext context) {
        IfSentence ifSentence = context.bodyStack.sentenceForIf();
        Label label = ifSentence.popLastIfFalse();
        context.registerLabelOnNextSequential(label);
        ifSentence.elseSentence();
    }
}
