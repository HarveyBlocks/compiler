package org.harvey.compiler.execute.test.version5.msg.stack;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.execute.test.version5.msg.Label;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-09 20:55
 */
@Setter
@Getter
public class ControlKey {
    private final Keyword control;
    // 用于在else上继续进行
    private Label ifOnFalse;
    // 用于 语句块成功执行的最后
    private Label skipExpressionAfter;
    // 被动
    private Label passiveReturnBack;
    // 主动
    private Label activeReturnBack;
    private boolean sentenceEndAfterControl;

    public ControlKey(Keyword control) {
        this.control = control;
    }

    public boolean confirm(Keyword token) {
        return this.control == token;
    }

    @Override
    public String toString() {
        return control.getValue();
    }
}
