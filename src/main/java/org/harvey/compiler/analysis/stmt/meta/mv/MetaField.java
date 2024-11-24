package org.harvey.compiler.analysis.stmt.meta.mv;

import lombok.Getter;
import org.harvey.compiler.common.entity.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:45
 */
@Getter
public class MetaField extends MetaValue {
    protected boolean embellishStatic;

    protected MetaField() {
        super();
        embellishStatic = false;
    }

    public static class Builder extends MetaValue.Builder<MetaField, MetaField.Builder> {
        public Builder() {
            super(new MetaField());
        }
    }
}
