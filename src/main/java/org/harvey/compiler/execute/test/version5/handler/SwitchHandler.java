package org.harvey.compiler.execute.test.version5.handler;


import org.harvey.compiler.execute.test.version5.msg.ControlContext;
import org.harvey.compiler.execute.test.version5.msg.stack.SwitchBody;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 15:13
 */
public interface SwitchHandler extends BodyStartHandler {
    /**
     * 不使context的body out, 不会pop break for switch
     */
    void switchTable(ControlContext context, SwitchBody switchBody);
}
