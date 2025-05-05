package org.harvey.compiler.execute.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * 表达式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:51
 */
public class Expression extends ArrayList<ExpressionElement> {
    // TODO NOT GOOD, 应该继承Expression类, 且不可读不可写
    public static final Expression EMPTY = new Expression();

    public Expression(Collection<? extends ExpressionElement> c) {
        super(c);
    }

    public Expression() {
        super();
    }

    public Expression(int initialCapacity) {
        super(initialCapacity);
    }

    public static Expression of(ComplexExpressionElement... element) {
        if (element == null) {
            return null;
        }
        Expression expression = new Expression();
        Collections.addAll(expression, element);
        return expression;
    }


}
