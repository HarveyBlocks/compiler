package org.harvey.compiler.declare.context;


/**
 * CallableContext的类型
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-05 21:49
 */
public enum CallableType {
    FUNCTION,
    CONSTRUCTOR,
    METHOD,
    OPERATOR, // 一定是METHOD
    CAST_OPERATOR, // 类型转换
}
