package org.harvey.compiler.analysis.calculate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.PropertyConstant;

import java.util.Objects;

/**
 * 运算符
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-15 21:00
 */
@AllArgsConstructor
@Getter
public enum Operator {
    AT_INDEX_PRE("[", 0, OperandCount.BINARY, Associativity.LEFT), // 取索引, 前缀
    AT_INDEX_POST("]", 0, OperandCount.BINARY, Associativity.LEFT), // 取索引, 后缀
    BRACKET_PRE("(", 0, OperandCount.BINARY, Associativity.LEFT), // 括号前缀
    BRACKET_POST(")", 0, OperandCount.BINARY, Associativity.LEFT),  // 括号后缀
    CALL_PRE("(", 0, OperandCount.BINARY, Associativity.LEFT), // 函数调用前缀
    CALL_POST(")", 0, OperandCount.BINARY, Associativity.LEFT),  // 函数调用后缀
    GET_MEMBER(String.valueOf(PropertyConstant.GET_MEMBER), 0, OperandCount.BINARY, Associativity.LEFT), // 成员调用

    // 符号
    MINUS("-", 1, OperandCount.UNARY, Associativity.RIGHT), // 右自增
    LEFT_INCREASING("++", 1, OperandCount.UNARY, Associativity.LEFT), // 左自增
    RIGHT_INCREASING("++", 1, OperandCount.UNARY, Associativity.RIGHT), // 右自增
    LEFT_DECREASING("--", 1, OperandCount.UNARY, Associativity.LEFT),  // 左自减
    RIGHT_DECREASING("--", 1, OperandCount.UNARY, Associativity.RIGHT), // 右自减
    NOT("!", 1, OperandCount.UNARY, Associativity.RIGHT), // 逻辑非
    BITWISE_NEGATION("~", 1, OperandCount.UNARY, Associativity.RIGHT), // 按位取反

    // 乘除取余
    DIVIDE("/", 2, OperandCount.BINARY, Associativity.LEFT), // 除
    MULTIPLY("*", 2, OperandCount.BINARY, Associativity.LEFT), // 乘
    REMAINDER("%", 2, OperandCount.BINARY, Associativity.LEFT), // 取余
    ADD("+", 3, OperandCount.BINARY, Associativity.LEFT), // 加
    SUBTRACT("-", 3, OperandCount.BINARY, Associativity.LEFT), // 减法
    BITWISE_LEFT_MOVE("<<", 4, OperandCount.BINARY, Associativity.LEFT),  // 位左移
    BITWISE_RIGHT_MOVE(">>", 4, OperandCount.BINARY, Associativity.LEFT),  // 位右移
    BITWISE_IGNORE_SIGN_LEFT_MOVE("<<<", 4, OperandCount.BINARY, Associativity.LEFT),  // 位无符号左移
    BITWISE_IGNORE_SIGN_RIGHT_MOVE(">>>", 4, OperandCount.BINARY, Associativity.LEFT),  // 位无符号右移

    // 比较
    LARGER(">", 5, OperandCount.BINARY, Associativity.LEFT), //
    LARGER_EQUALS(">=", 5, OperandCount.BINARY, Associativity.LEFT),//
    LESSER("<", 5, OperandCount.BINARY, Associativity.LEFT),//
    LESSER_EQUALS("<=", 5, OperandCount.BINARY, Associativity.LEFT), //
    EQUALS("==", 6, OperandCount.BINARY, Associativity.LEFT), //
    NOT_EQUALS("!=", 6, OperandCount.BINARY, Associativity.LEFT), //

    // 位操作
    BITWISE_AND("&", 7, OperandCount.BINARY, Associativity.LEFT), //
    BITWISE_XOR("^", 8, OperandCount.BINARY, Associativity.LEFT), //
    BITWISE_OR("|", 9, OperandCount.BINARY, Associativity.LEFT), //
    AND("&&", 10, OperandCount.BINARY, Associativity.LEFT), //
    OR("||", 11, OperandCount.BINARY, Associativity.LEFT), //

    // TODO
    CONDITION_CHECK("?", 12, OperandCount.BINARY, Associativity.RIGHT), //
    CONDITION_DECIDE(":", 12, OperandCount.BINARY, Associativity.RIGHT), //

    // 赋值和复合赋值
    ASSIGN("=", 13, OperandCount.BINARY, Associativity.RIGHT), //
    DIVIDE_ASSIGN("/=", 13, OperandCount.BINARY, Associativity.RIGHT), //
    MULTIPLY_ASSIGN("*=", 13, OperandCount.BINARY, Associativity.RIGHT), //
    REMAINDER_ASSIGN("%=", 13, OperandCount.BINARY, Associativity.RIGHT), //
    ADD_ASSIGN("+=", 13, OperandCount.BINARY, Associativity.RIGHT), //
    SUBTRACT_ASSIGN("-=", 13, OperandCount.BINARY, Associativity.RIGHT),//
    BITWISE_LEFT_MOVE_ASSIGN("<<=", 13, OperandCount.BINARY, Associativity.RIGHT), //
    BITWISE_IGNORE_SIGN_LEFT_MOVE_ASSIGN("<<<=", 13, OperandCount.BINARY, Associativity.RIGHT), //
    BITWISE_RIGHT_MOVE_ASSIGN(">>=", 13, OperandCount.BINARY, Associativity.RIGHT), //
    BITWISE_IGNORE_SIGN_RIGHT_MOVE_ASSIGN(">>>=", 13, OperandCount.BINARY, Associativity.RIGHT), //
    BITWISE_AND_ASSIGN("&=", 13, OperandCount.BINARY, Associativity.RIGHT),  //
    BITWISE_XOR_ASSIGN("^=", 13, OperandCount.BINARY, Associativity.RIGHT),  // ^=
    BITWISE_OR_ASSIGN("|=", 13, OperandCount.BINARY, Associativity.RIGHT),  // |=

    // 逗号运算符
    COMMA(",", 14, OperandCount.BINARY, Associativity.LEFT);
    // TODO ...More
    // 运算符的符号
    private final String name;
    // 优先级
    private final int priority;
    // 操作数
    private final OperandCount operandCount;
    // 结合性
    private final Associativity associativity;

    public boolean nameEquals(String name) {
        return Objects.equals(this.getName(), name);
    }
}
