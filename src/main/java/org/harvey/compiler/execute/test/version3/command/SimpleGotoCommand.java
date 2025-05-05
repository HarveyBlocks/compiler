package org.harvey.compiler.execute.test.version3.command;

import org.harvey.compiler.execute.test.version3.msg.Label;
import org.harvey.compiler.execute.test.version3.msg.ProgramCounter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 00:22
 */
@SuppressWarnings("DuplicatedCode")
public class SimpleGotoCommand implements GotoCommand {
    private final Label label;
    protected int jmp;

    public SimpleGotoCommand(Label label) {
        this.label = label;
    }

    @Override
    public int transitionToJump(ProgramCounter counter) {
        return this.jmp = counter.jumpToStep(label);
    }

    @Override
    public Label getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "jmp " + GotoCommand.jmpString(jmp) + " (" + label + ")";
    }
}
