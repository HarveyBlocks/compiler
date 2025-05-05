package org.harvey.compiler.execute.test.version1.element;

import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-04 21:27
 */
public class LambdaExpressionWrap extends ExpressionElement implements ItemString {
    public LambdaExpressionWrap(SourcePosition position, IdentifierString[] arguments) {
        super(position);
    }
}
