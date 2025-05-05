package org.harvey.compiler.execute.test.version5.command;

import org.harvey.compiler.execute.test.version5.msg.Label;
import org.harvey.compiler.execute.test.version5.msg.ProgramCounter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 00:05
 */
@SuppressWarnings("DuplicatedCode")
public interface GotoCommand extends SequentialCommand {
    static String jmpString(int jmp) {
        return (jmp >= 0 ? "+" : "") + jmp;
    }

    int transitionToJump(ProgramCounter counter);

    Label getLabel();
}
