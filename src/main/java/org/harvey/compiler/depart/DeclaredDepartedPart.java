package org.harvey.compiler.depart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.declare.Declarable;

/**
 * TODO
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
