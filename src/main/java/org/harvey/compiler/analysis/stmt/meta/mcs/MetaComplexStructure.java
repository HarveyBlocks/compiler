package org.harvey.compiler.analysis.stmt.meta.mcs;

import org.harvey.compiler.analysis.stmt.meta.MetaIdentifier;
import org.harvey.compiler.common.entity.SourceString;

/**
 * 复合体
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:40
 */
public abstract class MetaComplexStructure extends MetaIdentifier {
    public MetaComplexStructure(SourceString identifier) {
        super(identifier);
    }
}
