package org.harvey.compiler.analysis.stmt.meta.mv;

import lombok.Getter;
import org.harvey.compiler.common.entity.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:44
 */
@Getter
public class MetaLocalVariable extends MetaValue {
    protected MetaLocalVariable(SourcePosition sp, String identifier) {
        super();
    }

    public static class Builder extends MetaValue.Builder<MetaLocalVariable, Builder> {

        protected Builder(MetaLocalVariable metaValue) {
            super(metaValue);
        }

        public Builder(SourcePosition sp, String identifier) {
            super(new MetaLocalVariable(sp, identifier));
        }
    }
}
