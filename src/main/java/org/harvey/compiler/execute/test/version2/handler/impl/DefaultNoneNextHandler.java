package org.harvey.compiler.execute.test.version2.handler.impl;

import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version2.handler.NoneNextHandler;
import org.harvey.compiler.execute.test.version2.msg.ControlContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 23:21
 */
public class DefaultNoneNextHandler implements NoneNextHandler {
    @Override
    public void handle(ControlContext context) {
        if (context.bodyStack.empty()) {
            return;
        }
        if (!context.bodyStack.empty()) {
            throw new AnalysisControlException(context.now(), "expect }");
        }
    }
}
