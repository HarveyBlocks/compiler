package org.harvey.compiler.analysis.text.decomposer;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.exception.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 20:16
 */
public class CommitClearChecker implements TextDecomposer {
    @Override
    public SourceTextContext decompose(SourceString source) {
        switch (source.getType()) {
            case MULTI_LINE_COMMENTS:
            case SINGLE_LINE_COMMENTS:
                throw new CompilerException(source.getPosition()+", 依然存在注释");
            default:
                return null;
        }
    }
}
