package org.harvey.compiler.execute.test.version3.msg;


import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.test.version3.command.SequentialCommand;

import java.util.List;

/**
 * 获取到一个指令后立马加一, 指令在执行的时候, 指向下一条指令
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 00:05
 */
@SuppressWarnings("DuplicatedCode")
public class ProgramCounter {
    private final List<SequentialControlElement> sequential;
    private int counter;

    public ProgramCounter(List<SequentialControlElement> sequential) {
        this.sequential = sequential;
    }

    private int increase() {
        return counter++;
    }

    public int jumpToStep(Label label) {
        LineWarp labelLine = label.getLine();
        if (labelLine == null) {
            throw new CompilerException("Uncertain label");
        }
        return labelLine.getValue() - this.counter;
    }

    public SequentialCommand nextCommand() {
        SequentialControlElement element = sequential.get(counter);
        increase();
        return element.getCommand();
    }

    public boolean hasNext() {
        return counter < sequential.size();
    }
}
