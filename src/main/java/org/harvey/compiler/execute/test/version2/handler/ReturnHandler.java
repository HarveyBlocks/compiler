package org.harvey.compiler.execute.test.version2.handler;

import org.harvey.compiler.execute.test.version2.msg.ControlContext;

/**
 * TODO  
 * 
 * @date    2025-04-07 21:02
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public interface ReturnHandler extends OneControlSentenceHandler {
    @Override
    void nextIsExpression(ControlContext context);
}
