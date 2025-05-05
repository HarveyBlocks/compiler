package org.harvey.compiler.execute.test.version3.handler.impl;

import org.harvey.compiler.execute.test.version3.handler.BodyStartHandler;
import org.harvey.compiler.execute.test.version3.msg.ControlContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 21:41
 */
@SuppressWarnings("DuplicatedCode")
public class DefaultBodyStartHandler implements BodyStartHandler {

    @Override
    public void handle(ControlContext context) {
        context.bodyStack.bodyIn();
    }
}
