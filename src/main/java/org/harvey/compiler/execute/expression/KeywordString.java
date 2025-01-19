package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @date 2025-01-08 16:49
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class KeywordString extends ExpressionElement {
    // 大端
    private final Keyword keyword;

    public KeywordString(SourcePosition sp, Keyword keyword) {
        super(sp);
        this.keyword = keyword;
    }
}
