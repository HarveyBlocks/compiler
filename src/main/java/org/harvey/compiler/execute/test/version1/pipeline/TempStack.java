package org.harvey.compiler.execute.test.version1.pipeline;

import org.harvey.compiler.exception.self.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-03 23:30
 */
public class TempStack {
    public int top = 0;

    public int create() {
        return top++;
    }

    public int top() {
        return top;
    }

    /**
     * @return 消耗
     */
    public int deplete() {
        if (top == 0) {
            throw new CompilerException(new CompilerException("no temp to deplete"));
        }
        return top--;
    }
}
