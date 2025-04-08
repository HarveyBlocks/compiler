package org.harvey.compiler.execute.test.version2.command;

import org.harvey.compiler.execute.test.version2.msg.Label;
import org.harvey.compiler.execute.test.version2.msg.ProgramCounter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 00:05
 */
public interface GotoCommand extends SequentialCommand {
    int transitionToJump(ProgramCounter counter);

    Label getLabel();
}
