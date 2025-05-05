package org.harvey.compiler.execute.test.version5.msg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version3.command.SequentialCommand;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-09 23:55
 */
public class LocalVariableTableBuilder {
    private final Stack<Integer> counter = new Stack<>();
    private final Stack<Map<String, LocalVariableMessage>> nameOffset = new Stack<>();
    private int maxSize = 0;

    public void bodyIn() {
        nameOffset.push(new HashMap<>(nameOffset.peek()));
        counter.push(counter.peek()); // 拷贝到上一层
    }

    public void bodyOut() {
        nameOffset.pop();
        Integer size = counter.pop();
        if (size > maxSize) {
            maxSize = size;
        }
    }

    public LocalVariableMessage using(String name, SourcePosition position) {
        LocalVariableMessage msg = nameOffset.peek().get(name);
        if (msg == null) {
            throw new AnalysisControlException(position, "unknown identifier");
        }
        return msg;
    }

    public LocalVariableMessage declare(LocalVariableType type, String name, SourcePosition position) {
        if (nameOffset.peek().containsKey(name)) {
            throw new AnalysisControlException(position, "identifier has declared in local");
        }
        Map<String, LocalVariableMessage> peek = nameOffset.peek();
        LocalVariableMessage msg = new LocalVariableMessage(counter.peek(), type);
        peek.put(name, msg);
        counter.push(counter.pop() + type.getTypeSize());
        return msg;
    }

    public int localVariableTableSizeNeed() {
        return maxSize;
    }

    @AllArgsConstructor
    @Getter
    public static class LocalVariableMessage {
        private final int offset;
        private final LocalVariableType type;
    }

    @AllArgsConstructor
    public static class LoadLocalVariable implements SequentialCommand {
        private final int offset;
        private final LocalVariableType type;

        @Override
        public String toString() {
            return "load_local_variable_" + type.typeName() + " " + offset;
        }
    }

    @AllArgsConstructor
    public static class WriteLocalVariable implements SequentialCommand {
        private final int offset;
        private final LocalVariableType type;

        @Override
        public String toString() {
            return "temp_stack_top_write_local_variable_" + type.typeName() + " " + offset;
        }
    }
}
