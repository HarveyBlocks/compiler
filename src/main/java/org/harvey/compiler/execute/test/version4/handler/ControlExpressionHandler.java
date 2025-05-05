package org.harvey.compiler.execute.test.version4.handler;

import org.harvey.compiler.execute.test.version4.msg.ControlContext;
import org.harvey.compiler.io.source.SourceString;

import java.util.ListIterator;
import java.util.function.Predicate;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-09 00:06
 */
@SuppressWarnings("DuplicatedCode")
public interface ControlExpressionHandler extends ExecutableControlHandler, ExpressionHandler {
    /**
     * @param context       加入表达式
     * @param source        表达式, 被分离
     * @param stopCondition 被跳过
     */
    void normalHandle(
            ControlContext context, ListIterator<SourceString> source, Predicate<SourceString> stopCondition);
}
