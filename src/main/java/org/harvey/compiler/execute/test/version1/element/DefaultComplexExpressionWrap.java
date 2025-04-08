package org.harvey.compiler.execute.test.version1.element;

import lombok.Getter;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * {@link ComplexExpressionWrap} 默认实现
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-04 16:31
 */
@Getter
public class DefaultComplexExpressionWrap extends ExpressionElement implements
        ComplexExpressionWrap {
    private Expression expression;

    public DefaultComplexExpressionWrap(SourcePosition position) {
        super(position);
    }

    public boolean finished() {
        return expression != null;
    }

    @Override
    public void setExpression(Expression expression) {
        this.expression = expression;
        afterSet();
    }

    protected void afterSet() {
        // 钩子方法
    }
}
