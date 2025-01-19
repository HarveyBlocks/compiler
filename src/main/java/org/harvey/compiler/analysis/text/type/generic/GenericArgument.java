package org.harvey.compiler.analysis.text.type.generic;

import lombok.Getter;
import org.harvey.compiler.io.source.SourceString;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-04 11:46
 */
@Getter
public class GenericArgument {
    private final SourceString name;
    private final GenericType lower;
    private final GenericType upper;
    private final GenericType defaultGeneric;

    public GenericArgument(SourceString name, GenericType lower, GenericType upper, GenericType defaultGeneric) {
        this.name = name;
        this.lower = lower;
        this.upper = upper;
        this.defaultGeneric = defaultGeneric;
    }
}
