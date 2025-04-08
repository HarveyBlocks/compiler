package org.harvey.compiler.execute.test.version2.handler.impl;

import org.harvey.compiler.execute.test.version2.handler.ControlExpressionHandler;
import org.harvey.compiler.execute.test.version2.msg.ControlContext;
import org.harvey.compiler.execute.test.version2.command.DefaultSequentialCommand;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;

/**
 * 都是跳过一个括号的
 * 不需要最后context.nowHandler(this);
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:00
 */
public class ConditionExpressionHandler implements ControlExpressionHandler {

    @Override
    public void handle(ControlContext context) {
        // TODO
        // 被()包围
        SourceTextContext condition = SourceTextContext.skipNest(context, "(", ")", true);
        condition.removeFirst();
        condition.removeLast();
        for (SourceString s : condition) {
            context.addSequential(new DefaultSequentialCommand("st_push_value " + s.getValue()));
        }
        // throw new CompilerException(new UnfinishedException());
    }
}
