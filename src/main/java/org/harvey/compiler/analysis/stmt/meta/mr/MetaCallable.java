package org.harvey.compiler.analysis.stmt.meta.mr;

import org.harvey.compiler.analysis.stmt.meta.MetaIdentifier;
import org.harvey.compiler.common.entity.SourceString;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:43
 */
public abstract class MetaCallable extends MetaIdentifier {
    public MetaCallable(SourceString identifier) {
        super(identifier);
    }
}
