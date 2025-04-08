package org.harvey.compiler.text.decomposer;

import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.Set;

/**
 * 检查{@link SourceType}是否正确
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-09 16:14
 */
public class CheckTypeDecomposer implements TextDecomposer {
    private final Set<SourceType> illegalType;

    public CheckTypeDecomposer(Set<SourceType> illegalType) {
        this.illegalType = illegalType;
    }

    @Override
    public SourceTextContext decompose(SourceString source) {
        if (!illegalType.contains(source.getType())) {
            throw new CompilerException("illegal type of" + source.getType() + ", and source: " + source);
        }
        return null;
    }
}
