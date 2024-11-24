package org.harvey.compiler.analysis.text.type;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 21:47
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CallableMultipleReturnType extends SourceType {
    private final SourceType[] types;

    public CallableMultipleReturnType(SourceType... types) {
        this.types = types;
    }
}
