package org.harvey.compiler.execute.test.version1.handler;

import org.harvey.compiler.execute.expression.ConstantString;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.io.source.SourceString;

/**
 * 常量的处理
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:49
 */
@SuppressWarnings("unused")
public class ConstantExpressionHandler implements ExpressionHandler {


    @Override
    public boolean handle(ExpressionContext context) {
        SourceString nextSource = context.next();
        ConstantString constantString = ConstantString.constantString(nextSource);
        if (constantString == null) {
            context.previousMove();
            return false;
        } else {
            context.add(constantString);
            return true;
        }
    }


}
