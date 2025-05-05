package org.harvey.compiler.execute.test.version3.stack;


import java.util.Stack;

@SuppressWarnings("DuplicatedCode")
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

    public AfterControlSentence sentenceAfterControl() {
        return new AfterControlSentence(stack.peek().sentenceAfterControlFrame);
    }


}
