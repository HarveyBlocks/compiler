package org.harvey.compiler.analysis.text.type.generic;

import lombok.Getter;
import org.harvey.compiler.io.source.SourceString;

import java.util.Objects;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-04 11:50
 */
@Getter
public class NormalType {
    private final SourceString name;

    private final NormalType parent;

    public NormalType(SourceString name, NormalType parent) {
        this.name = name;
        this.parent = parent;
    }

    public NormalType(NormalType origin) {
        this.name = origin.getName();
        this.parent = origin.getParent();
    }

    public NormalType(SourceString name) {
        this(name, null);
    }

    public static boolean nameEquals(NormalType type, NormalType upper) {
        if (type == upper) {
            return true;
        }
        return Objects.equals(type.getName(), upper.getName());
    }
}
