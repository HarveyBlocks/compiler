package org.harvey.compiler.execute.test.version4.msg;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.execute.test.version4.command.SequentialCommand;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:11
 */
@SuppressWarnings("DuplicatedCode")
public class SequentialControlElement {
    private final Collection<Label> labels;
    private final LineWarp commandLine;
    @Getter
    private SequentialCommand command;
    @Setter
    private int sourceLine;

    public SequentialControlElement(int commandLine, SequentialCommand command, int sourceLine) {
        this.commandLine = new LineWarp(commandLine);
        this.labels = new ArrayList<>();
        this.command = command;
        this.sourceLine = sourceLine;
    }

    public static SequentialControlElement empty(int line) {
        return new SequentialControlElement(line, null, 0);
    }

    public void registerLabel(Label label) {
        label.setLine(commandLine.getValue());
        this.labels.add(label);
    }

    public void registerLabel(Collection<Label> label) {
        label.forEach(l -> l.setLine(commandLine.getValue()));
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
        String labels = this.labels.isEmpty() ? "" : sb + "\n";
        return String.format("%s\t[%02d-%02d] %s", labels, sourceLine, commandLine.getValue(), command);
    }

    public void instance(SequentialCommand command, int sourceLine) {
        this.command = command;
        this.sourceLine = sourceLine;
    }

}
