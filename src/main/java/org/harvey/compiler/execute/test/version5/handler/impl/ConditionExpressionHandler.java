package org.harvey.compiler.execute.test.version5.handler.impl;

import org.harvey.compiler.execute.test.version5.command.DefaultSequentialCommand;
import org.harvey.compiler.execute.test.version5.handler.ControlExpressionHandler;
import org.harvey.compiler.execute.test.version5.msg.ControlContext;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.ListIterator;
import java.util.function.Predicate;

/**
 * 都是跳过一个括号的
 * 不需要最后context.nowHandler(this);
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:00
 */
@SuppressWarnings("DuplicatedCode")
public class ConditionExpressionHandler implements ControlExpressionHandler {

    @Override
    public void handle(ControlContext context) {
        // TODO
        // 被()包围
        SourceTextContext condition = SourceTextContext.skipNest(context, "(", ")", true);
        condition.removeFirst();
        condition.removeLast();
        normalHandle(context, condition.listIterator(), null);
        // throw new CompilerException(new UnfinishedException());
    }

    @Override
    public void normalHandle(
            ControlContext context,
            ListIterator<SourceString> source,
            Predicate<SourceString> stopCondition) {
        while (source.hasNext()) {
            SourceString next = source.next();
            context.addSequential(new DefaultSequentialCommand("st_push_value " + next.getValue()));
        }
    }
}
