package org.harvey.compiler.execute.test.version5.msg.stack;

import org.harvey.compiler.exception.self.CompilerException;

import java.util.Stack;

/**
 * 存储信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 21:08
 */
@SuppressWarnings("DuplicatedCode")
class SentenceAfterControlFrame {
    Stack<ControlKey> controlKeyStack = new Stack<>();
    boolean startSentence;
    boolean stillSentence;

    SentenceAfterControlFrame() {
    }

    public void reset() {
        stillSentence = false;
        startSentence = false;
        if (!this.controlKeyStack.empty()) {
            throw new CompilerException("未处理的controlKey");
        }
    }


}
