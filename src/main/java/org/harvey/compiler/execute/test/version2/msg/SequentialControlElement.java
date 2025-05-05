package org.harvey.compiler.execute.test.version2.msg;

import lombok.Getter;
import org.harvey.compiler.execute.test.version2.command.SequentialCommand;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:11
 */
public class SequentialControlElement {
    private final Collection<Label> labels;
    private final LineWarp line;
    @Getter
    private SequentialCommand command;

    public SequentialControlElement(int line, SequentialCommand command) {
        this.line = new LineWarp(line);
        this.labels = new ArrayList<>();
        this.command = command;
    }

    public static SequentialControlElement empty(int line) {
        return new SequentialControlElement(line, null);
    }

    public void registerLabel(Label label) {
        label.setLine(line.getValue());
        this.labels.add(label);
    }

    public void registerLabel(Collection<Label> label) {
        label.forEach(l -> l.setLine(line.getValue()));
        this.labels.addAll(label);
    }

    public boolean nullCommand() {
        return command == null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Label label : labels) {
            sb.append(label).append(":");
        }
        return String.format("%s\t[%04d] %s", labels.isEmpty() ? "" : sb + "\n", line, command);
    }

    public void setCommand(SequentialCommand command) {
        this.command = command;
    }

}
