package org.harvey.compiler.text.depart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.analysis.Declarable;
import org.harvey.compiler.text.context.SourceTextContext;

/**
 * 根据;, `{}`分解结构
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 00:30
 */
@Getter
@AllArgsConstructor
public class DeclaredDepartedPart {
    private final Declarable statement;
    private final SourceTextContext body;

}
