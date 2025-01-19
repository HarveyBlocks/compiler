package org.harvey.compiler.execute.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO  
 *
 * @date 2025-01-09 04:05
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class LambdaReferenceExpression extends ComplexExpression {
    private final IdentifierString[] arguments;
    /**
     * 1. null 表示是一行的, 其函数体也是表达式, 也能被解析
     * 2. 否则, 去除了{}的内部表达式
     */
    private final int reference;
}
