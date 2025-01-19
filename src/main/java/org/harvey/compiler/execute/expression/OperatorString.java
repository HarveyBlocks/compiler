package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @date 2025-01-08 16:46
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class OperatorString extends ExpressionElement {
    private final Operator value;

    public OperatorString(SourcePosition sp, Operator value) {
        super(sp);
        this.value = value;
    }
}
