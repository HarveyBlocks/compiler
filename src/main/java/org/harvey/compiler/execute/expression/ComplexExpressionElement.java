package org.harvey.compiler.execute.expression;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 例如lambda表达式之类的
 */
@Getter
@Setter
public class ComplexExpressionElement extends ExpressionElement {
    private ComplexExpression expression;

    public ComplexExpressionElement(SourcePosition sp, ComplexExpression expression) {
        super(sp);
        this.expression = expression;
    }
}
