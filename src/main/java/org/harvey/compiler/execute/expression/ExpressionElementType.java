package org.harvey.compiler.execute.expression;

/**
 * 在表达式中的成员的类型, 用于持久化
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-21 22:50
 */
public enum ExpressionElementType {
    IDENTIFIER, KEYWORD, OPERATOR, CONSTANT, FULL_IDENTIFIER, IDENTIFIER_REFERENCE, LOCAL_VARIABLE, COMPLEX
}
