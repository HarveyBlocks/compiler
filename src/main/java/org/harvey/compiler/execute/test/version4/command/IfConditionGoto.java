package org.harvey.compiler.execute.test.version4.command;

import org.harvey.compiler.execute.test.version4.msg.Label;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 00:07
 */
@SuppressWarnings("DuplicatedCode")
public class IfConditionGoto extends SimpleGotoCommand {
    private final boolean condition;

    private IfConditionGoto(boolean condition, Label label) {
        super(label);
        this.condition = condition;
    }

    public static IfConditionGoto onTrue(Label label) {
        return new IfConditionGoto(true, label);
    }

    public static IfConditionGoto onFalse(Label label) {
        return new IfConditionGoto(false, label);
    }


    @Override
    public String toString() {
        return "if_" + condition + "_" + super.toString();
    }
}
