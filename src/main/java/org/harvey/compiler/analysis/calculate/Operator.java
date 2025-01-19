package org.harvey.compiler.analysis.calculate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.common.SourceFileConstant;

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
    // 用Object[]来声明数组? 还是用Array<int>来声明数组?MultidimensionalArray<int> mdArr = new(int...维度长度);
    // Array<Array<int>> 也是二维数组, 但是区别在于存数组指针, 且可以不等长
    // Array<int> arr = new(length);
    // Array<int> arr = new(length,defaultValue);
    // Array<int> arr = new(length,index->{return index++;});// 初始化
    // 这样好吗?
    CALLABLE_DECLARE("()", 8, OperandCount.UNARY, Associativity.RIGHT), // 重载
    ARRAY_DECLARE("[]", 8, OperandCount.UNARY, Associativity.RIGHT), // 重载
    MULTIPLE_TYPE("...", 9, OperandCount.UNARY, Associativity.RIGHT), // 不定参数
    GENERIC_LIST_PRE("<", 9, OperandCount.UNARY, Associativity.RIGHT), // 声明泛型, 前缀
    GENERIC_LIST_POST(">", 9, OperandCount.UNARY, Associativity.RIGHT), // 声明泛型, 前缀
    CALL_PRE("(", 9, OperandCount.BINARY, Associativity.RIGHT), // 函数调用前缀
    CALL_POST(")", 9, OperandCount.BINARY, Associativity.RIGHT),  // 函数调用后缀
    AT_INDEX_PRE("[", 10, OperandCount.BINARY, Associativity.LEFT), // 取索引, 前缀
    AT_INDEX_POST("]", 10, OperandCount.BINARY, Associativity.LEFT), // 取索引, 后缀
    BRACKET_PRE("(", 10, OperandCount.NONE, Associativity.LEFT), // 括号前缀
    BRACKET_POST(")", 10, OperandCount.NONE, Associativity.LEFT),  // 括号后缀
    GET_MEMBER(String.valueOf(SourceFileConstant.GET_MEMBER), 10, OperandCount.BINARY, Associativity.LEFT), // 成员调用

    // 符号
    NEGATIVE("-", 11, OperandCount.UNARY, Associativity.RIGHT), // 负
    POSITIVE("+", 11, OperandCount.UNARY, Associativity.RIGHT), // 正
    LEFT_INCREASING("++", 11, OperandCount.UNARY, Associativity.LEFT), // 左自增
    RIGHT_INCREASING("++", 11, OperandCount.UNARY, Associativity.RIGHT), // 右自增
    LEFT_DECREASING("--", 11, OperandCount.UNARY, Associativity.LEFT),  // 左自减
    RIGHT_DECREASING("--", 11, OperandCount.UNARY, Associativity.RIGHT), // 右自减
    NOT("!", 11, OperandCount.UNARY, Associativity.RIGHT), // 逻辑非
    BITWISE_NEGATION("~", 11, OperandCount.UNARY, Associativity.RIGHT), // 按位取反

    // 乘除取余
    DIVIDE("/", 12, OperandCount.BINARY, Associativity.LEFT), // 除
    MULTIPLY("*", 12, OperandCount.BINARY, Associativity.LEFT), // 乘
    REMAINDER("%", 12, OperandCount.BINARY, Associativity.LEFT), // 取余
    ADD("+", 13, OperandCount.BINARY, Associativity.LEFT), // 加
    SUBTRACT("-", 13, OperandCount.BINARY, Associativity.LEFT), // 减法
    BITWISE_LEFT_MOVE("<<", 14, OperandCount.BINARY, Associativity.LEFT),  // 位左移
    BITWISE_RIGHT_MOVE(">>", 14, OperandCount.BINARY, Associativity.LEFT),  // 位右移


    // 比较
    LARGER(">", 15, OperandCount.BINARY, Associativity.LEFT), //
    LARGER_EQUALS(">=", 15, OperandCount.BINARY, Associativity.LEFT),//
    LESS("<", 15, OperandCount.BINARY, Associativity.LEFT),//
    LESS_EQUALS("<=", 15, OperandCount.BINARY, Associativity.LEFT), //
    // 偏序关系 obj is Type
    IS("is", 16, OperandCount.BINARY, Associativity.LEFT), // a is b is c
    IN("in", 17, OperandCount.BINARY, Associativity.LEFT), // a is b in c
    EQUALS("==", 18, OperandCount.BINARY, Associativity.LEFT), //
    NOT_EQUALS("!=", 18, OperandCount.BINARY, Associativity.LEFT), //

    // 位操作
    BITWISE_AND("&", 19, OperandCount.BINARY, Associativity.LEFT), //
    BITWISE_XOR("^", 20, OperandCount.BINARY, Associativity.LEFT), //
    BITWISE_OR("|", 21, OperandCount.BINARY, Associativity.LEFT), //
    AND("&&", 22, OperandCount.BINARY, Associativity.LEFT), //
    OR("||", 23, OperandCount.BINARY, Associativity.LEFT), //


    CONDITION_CHECK("?", 24, OperandCount.BINARY, Associativity.RIGHT), //
    CONDITION_DECIDE(":", 24, OperandCount.BINARY, Associativity.RIGHT), //

    // 赋值和复合赋值
    ASSIGN("=", 25, OperandCount.BINARY, Associativity.RIGHT), //
    DIVIDE_ASSIGN("/=", 25, OperandCount.BINARY, Associativity.RIGHT), //
    MULTIPLY_ASSIGN("*=", 25, OperandCount.BINARY, Associativity.RIGHT), //
    REMAINDER_ASSIGN("%=", 25, OperandCount.BINARY, Associativity.RIGHT), //
    ADD_ASSIGN("+=", 25, OperandCount.BINARY, Associativity.RIGHT), //
    SUBTRACT_ASSIGN("-=", 25, OperandCount.BINARY, Associativity.RIGHT),//
    BITWISE_LEFT_MOVE_ASSIGN("<<=", 25, OperandCount.BINARY, Associativity.RIGHT), //
    BITWISE_RIGHT_MOVE_ASSIGN(">>=", 25, OperandCount.BINARY, Associativity.RIGHT), //
    BITWISE_AND_ASSIGN("&=", 25, OperandCount.BINARY, Associativity.RIGHT),  //
    BITWISE_XOR_ASSIGN("^=", 25, OperandCount.BINARY, Associativity.RIGHT),  // ^=
    BITWISE_OR_ASSIGN("|=", 25, OperandCount.BINARY, Associativity.RIGHT),  // |=

    // 逗号运算符
    COMMA(",", 26, OperandCount.BINARY, Associativity.LEFT),//
    LAMBDA("->", 27, OperandCount.BINARY, Associativity.NONE),//
    // foreach的时候, case 的时候
    // ?:的时候....
    // COLON(":", 27, OperandCount.NONE, Associativity.NONE)
    ;
    // TODO ...More
    // 运算符的符号
    private final String name;
    // 优先级
    private final int priority;
    // 操作数
    private final OperandCount operandCount;
    // 结合性
    private final Associativity associativity;

    public static Operator fromKeyword(Keyword keyword) {
        return Keywords.OPERATOR_KEYWORD_MAP.get(keyword);
    }

    public boolean nameEquals(String name) {
        return Objects.equals(this.getName(), name);
    }

    public boolean operandIs(OperandCount count) {
        return operandCount == count;
    }

    public boolean associativityIs(Associativity associativity) {
        return this.associativity == associativity;
    }
}
