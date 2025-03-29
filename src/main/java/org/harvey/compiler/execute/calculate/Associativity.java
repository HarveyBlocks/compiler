package org.harvey.compiler.execute.calculate;


/**
 * 结合性
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-15 20:57
 */
public enum Associativity {
    // 此运算符讨论结合性没有意义
    NONE,
    // 左结合(自左向右)
    LEFT,
    // 右结合(自右向左)
    RIGHT,
}
