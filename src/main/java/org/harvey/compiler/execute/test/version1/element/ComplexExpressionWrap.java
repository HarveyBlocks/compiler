package org.harvey.compiler.execute.test.version1.element;

import org.harvey.compiler.execute.expression.Expression;

public interface ComplexExpressionWrap extends ItemString {
    boolean finished();

    Expression getExpression();

    void setExpression(Expression expression);
}