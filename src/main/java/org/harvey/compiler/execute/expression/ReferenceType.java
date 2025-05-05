package org.harvey.compiler.execute.expression;

/**
 * 表达式中的引用的引用类型
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-11 21:42
 */
public enum ReferenceType {
    KEYWORD, OPERATOR, GENERIC_IDENTIFIER, ALIAS_GENERIC_IDENTIFIER, IDENTIFIER, CONSTRUCTOR, CAST_OPERATOR, IGNORE
}
