package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.IdentifierString;

/**
 * TODO  
 *
 * @date 2025-01-10 13:22
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class DeclareExecutable extends SequentialExecutable {
    private final boolean markedConst;
    private final Expression type;
    private final IdentifierString identifier;
}
