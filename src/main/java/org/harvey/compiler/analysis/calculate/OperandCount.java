package org.harvey.compiler.analysis.calculate;


/**
 * 操作数
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-15 20:59
 */
public enum OperandCount {
    // 不需要操作数的运算
    NONE,
    // 单元运算符
    UNARY,
    // 二元运算符
    BINARY,
    // 三元运算符
    TERNARY
}
