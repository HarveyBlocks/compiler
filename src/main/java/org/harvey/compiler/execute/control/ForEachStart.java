package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.io.source.SourceString;

/**
 * TODO  
 *
 * @date 2025-01-08 23:40
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class ForEachStart extends BodyStart {
    private final boolean markConst;
    private final boolean markFinal;
    private final Expression itemType;
    private final SourceString itemIdentifier;
    // range(1,2)
    // map.keys();
    private final Expression list;
}
