package org.harvey.compiler.execute.test.version3.msg;

import lombok.Getter;
import org.harvey.compiler.exception.self.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:07
 */
@SuppressWarnings("DuplicatedCode")
@Getter
public class Label {
    private final String name;

    private LineWarp line = null;

    public Label(int id) {
        name = "L" + id;
    }

    public void setLine(int line) {
        if (this.line == null) {
            this.line = new LineWarp(line);
            return;
        }
        if (line == this.line.getValue()) {
            return;
        }
        throw new CompilerException("goto uncertain label: " + name);
    }

    @Override
    public String toString() {
        return name;
    }
}
