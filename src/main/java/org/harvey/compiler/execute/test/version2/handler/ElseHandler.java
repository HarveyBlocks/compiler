package org.harvey.compiler.execute.test.version2.handler;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.test.version2.msg.ControlContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:01
 */
public interface ElseHandler extends ExecutableControlHandler {
    @Override
    default void nextIsDeclareType(ControlContext context) {
        throw new AnalysisExpressionException(context.now(), "not expect declare in else sentence");
    }

    @Override
    default void nextIsElse(ControlContext context) {
        throw new AnalysisExpressionException(context.now(), "not expect declare in else sentence");
    }

    @Override
    default void notHaveNext(ControlContext context) {
        throw new AnalysisExpressionException(context.now(), "not expect declare in else sentence");
    }
}
