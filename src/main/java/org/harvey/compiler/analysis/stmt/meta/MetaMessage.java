package org.harvey.compiler.analysis.stmt.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.harvey.compiler.common.entity.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 22:34
 */
@Data
@ToString
@AllArgsConstructor
public abstract class MetaMessage {
    protected SourcePosition declare;

    public MetaMessage() {
        declare = null;
    }
}
