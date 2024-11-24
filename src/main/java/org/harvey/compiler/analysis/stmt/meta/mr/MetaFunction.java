package org.harvey.compiler.analysis.stmt.meta.mr;

import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.exception.CompilerException;

/**
 * GlobalFunction = Function !=Method
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:40
 */
public class MetaFunction extends MetaCallable {
    public MetaFunction(SourceString identifier) {
        super(identifier);
    }
}
