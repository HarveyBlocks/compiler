package org.harvey.compiler.execute.test.version4.handler;


import org.harvey.compiler.io.source.SourceString;

import java.util.List;

/**
 * TODO
 * 不是控制结构的接口, 算出常量值
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 16:05
 */
public interface ConstantExpressionHandler extends ExpressionHandler {
    ConstantResult calculate(List<SourceString> constantExpression);

    interface ConstantResult {
        String show();
    }
}
