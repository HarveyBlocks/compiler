package org.harvey.compiler.execute.test.version2.stack;

import java.util.Stack;

public class BodyStack {
    private final Stack<BodyStackFrame> stack = new Stack<>();

    public boolean empty() {
        return stack.empty();
    }

    public void bodyIn() {
        this.stack.push(new BodyStackFrame());
    }

    public void bodyOut() {
        this.stack.pop();
    }

    public IfSentence sentenceForIf() {
        return new IfSentence(stack.peek().oneSentenceFrame);
    }


}
