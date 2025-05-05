package org.harvey.compiler.execute.test.version5.msg.stack;


import org.harvey.compiler.execute.test.version5.msg.Label;
import org.harvey.compiler.execute.test.version5.msg.LocalVariableTableBuilder;

import java.util.Stack;

@SuppressWarnings("DuplicatedCode")
public class BodyStack {
    public final LocalVariableTableBuilder localVariableTableBuilder = new LocalVariableTableBuilder();
    private final Stack<BodyStackFrame> stack = new Stack<>();

    public boolean empty() {
        return stack.empty();
    }

    public void bodyIn() {
        this.stack.push(new BodyStackFrame());
        localVariableTableBuilder.bodyIn();
    }

    public void bodyOut() {
        this.stack.pop();
        localVariableTableBuilder.bodyOut();
    }


    public AfterControlSentence sentenceAfterControl() {
        return new AfterControlSentence(stack.peek().sentenceAfterControlFrame);
    }


    public void switchBodyIn(Label switchTable, Label switchBreak) {
        BodyStackFrame frame = new BodyStackFrame();
        this.stack.push(frame);
        frame.switchBody = new SwitchBody(switchTable, switchBreak);
    }

    public boolean isSwitchBody() {
        return this.stack.peek().switchBody == null;
    }

    public SwitchBody switchBody() {
        return this.stack.peek().switchBody;
    }

}
