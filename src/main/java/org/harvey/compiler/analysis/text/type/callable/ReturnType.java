package org.harvey.compiler.analysis.text.type.callable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.SourceVariableDeclare;

import java.util.Collections;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-05 15:31
 */
@Getter
public class ReturnType {
    public static final ReturnType NONE = new NoneReturnType();
    private final List<SourceVariableDeclare.LocalType> types;

    public ReturnType(SourceVariableDeclare.LocalType type) {
        types = List.of(type);
    }

    public ReturnType(List<SourceVariableDeclare.LocalType> types) {
        this.types = types;
    }

    public boolean isSingle() {
        return types.size() == 1;
    }

    public boolean isNone() {
        return types.isEmpty();
    }

    @Override
    public String toString() {
        return String.valueOf(isSingle() ? types.get(0) : types);
    }

    private static class NoneReturnType extends ReturnType {
        private NoneReturnType() {
            super(Collections.emptyList());
        }
    }
}
