package org.harvey.compiler.execute.test.version4.handler.impl;

import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version4.handler.DefaultCaseHandler;
import org.harvey.compiler.execute.test.version4.msg.ControlContext;
import org.harvey.compiler.execute.test.version4.msg.Label;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 15:16
 */
public class DefaultCaseHandlerImpl implements DefaultCaseHandler {
    @Override
    public void handle(ControlContext context) {
        if (!context.bodyStack.isSwitchBody()) {
            throw new AnalysisControlException(context.now(), "except in switch body");
        }
        Label defaultLabel = context.createLabel();
        context.registerLabelOnNextSequential(defaultLabel);
        context.bodyStack.switchBody().setDefaultLabel(defaultLabel);
    }
}
