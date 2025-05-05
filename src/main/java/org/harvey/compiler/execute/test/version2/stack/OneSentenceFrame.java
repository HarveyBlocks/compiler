package org.harvey.compiler.execute.test.version2.stack;

import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.test.version2.msg.Label;

import java.util.Stack;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 21:08
 */
class OneSentenceFrame {
    Label skipOtherElse;
    Stack<Label> lastIfFalse = new Stack<>();
    boolean startSentenceIf;
    boolean stillSentence;

    OneSentenceFrame() {
    }

    public void reset() {
        stillSentence = false;
        startSentenceIf = false;
        skipOtherElse = null;
        if (!this.lastIfFalse.empty()) {
            throw new CompilerException("未处理的last if false label");
        }
        if (this.skipOtherElse != null) {
            throw new CompilerException("未处理的skip other else");
        }
    }
}
