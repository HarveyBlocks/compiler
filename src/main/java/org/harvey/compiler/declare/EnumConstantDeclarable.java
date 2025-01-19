package org.harvey.compiler.declare;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.io.source.SourceString;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-02 16:25
 */
@Getter
@AllArgsConstructor
public class EnumConstantDeclarable {
    private final SourceString name;
    /**
     * 去除了括号
     */
    private final SourceTextContext argumentList;
}
