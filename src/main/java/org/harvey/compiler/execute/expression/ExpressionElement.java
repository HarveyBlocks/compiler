package org.harvey.compiler.execute.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 在表达式中的从成员
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:45
 */
@AllArgsConstructor
@Getter
public abstract class ExpressionElement implements IExpressionElement {

    private SourcePosition position;


}
