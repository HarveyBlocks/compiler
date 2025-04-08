package org.harvey.compiler.execute.calculate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.analysis.Keywords;

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
    CALLABLE_DECLARE("()", 8, OperandCount.UNARY, Associativity.LEFT), // 重载
    ARRAY_DECLARE("[]", 8, OperandCount.UNARY, Associativity.LEFT), // 重载
    MULTIPLE_TYPE("...", 8, OperandCount.UNARY, Associativity.RIGHT), // 不定参数
    // 
    GENERIC_LIST_PRE("[", 16, OperandCount.NONE, Associativity.LEFT), // 声明泛型, 前缀
    GENERIC_LIST_POST("]", 16, OperandCount.NONE, Associativity.LEFT), // 声明泛型, 前缀

    CALL_PRE("(", 16, OperandCount.NONE, Associativity.LEFT), // 函数调用前缀
    CALL_POST(")", 16, OperandCount.NONE, Associativity.LEFT),  // 函数调用后缀
    ARRAY_AT_PRE("[", 24, OperandCount.NONE, Associativity.LEFT), // 取索引, 前缀
    ARRAY_AT_POST("]", 24, OperandCount.NONE, Associativity.LEFT), // 取索引, 后缀
    PARENTHESES_PRE("(", 24, OperandCount.NONE, Associativity.LEFT), // 括号前缀
    PARENTHESES_POST(")", 24, OperandCount.NONE, Associativity.LEFT),  // 括号后缀
    GET_MEMBER(String.valueOf(SourceFileConstant.GET_MEMBER), 24, OperandCount.BINARY, Associativity.LEFT), // 成员调用

    // 符号
    NEGATIVE("-", 32, OperandCount.UNARY, Associativity.RIGHT), // 负
    POSITIVE("+", 32, OperandCount.UNARY, Associativity.RIGHT), // 正
    LEFT_INCREASING("++", 32, OperandCount.UNARY, Associativity.RIGHT), // 左自增
    RIGHT_INCREASING("++", 32, OperandCount.UNARY, Associativity.LEFT), // 右自增 a ++ ++ ++
    LEFT_DECREASING("--", 32, OperandCount.UNARY, Associativity.RIGHT),  // 左自减
    RIGHT_DECREASING("--", 32, OperandCount.UNARY, Associativity.LEFT), // 右自减
    NOT("!", 32, OperandCount.UNARY, Associativity.RIGHT), // 逻辑非
    BITWISE_NEGATION("~", 32, OperandCount.UNARY, Associativity.RIGHT), // 按位取反

    // 乘除取余
    DIVIDE("/", 40, OperandCount.BINARY, Associativity.LEFT), // 除
    MULTIPLY("*", 40, OperandCount.BINARY, Associativity.LEFT), // 乘
    REMAINDER("%", 40, OperandCount.BINARY, Associativity.LEFT), // 取余
    ADD("+", 48, OperandCount.BINARY, Associativity.LEFT), // 加
    SUBTRACT("-", 48, OperandCount.BINARY, Associativity.LEFT), // 减法
    BITWISE_LEFT_MOVE("<<", 56, OperandCount.BINARY, Associativity.LEFT),  // 位左移
    BITWISE_RIGHT_MOVE(">>", 56, OperandCount.BINARY, Associativity.LEFT),  // 位右移


    // 比较
    LARGER(">", 64, OperandCount.BINARY, Associativity.LEFT),
    LARGER_EQUALS(">=", 64, OperandCount.BINARY, Associativity.LEFT),
    LESS("<", 64, OperandCount.BINARY, Associativity.LEFT),
    LESS_EQUALS("<=", 64, OperandCount.BINARY, Associativity.LEFT),
    // 偏序关系 obj is Type
    IS("is", 72, OperandCount.BINARY, Associativity.LEFT), // a is b is c
    IN("in", 80, OperandCount.BINARY, Associativity.LEFT), // 'a' is 'b' collectionIn 'c'
    EQUALS("==", 88, OperandCount.BINARY, Associativity.LEFT),
    NOT_EQUALS("!=", 88, OperandCount.BINARY, Associativity.LEFT),

    // 位操作
    BITWISE_AND("&", 96, OperandCount.BINARY, Associativity.LEFT),
    BITWISE_XOR("^", 104, OperandCount.BINARY, Associativity.LEFT),
    BITWISE_OR("|", 112, OperandCount.BINARY, Associativity.LEFT),
    // 逻辑表达式
    AND("&&", 120, OperandCount.BINARY, Associativity.LEFT),
    OR("||", 128, OperandCount.BINARY, Associativity.LEFT),


    CONDITION_CHECK("?", 136, OperandCount.BINARY, Associativity.RIGHT),
    CONDITION_DECIDE(":", 136, OperandCount.BINARY, Associativity.RIGHT),

    // 赋值和复合赋值
    ASSIGN("=", 144, OperandCount.BINARY, Associativity.RIGHT),
    DIVIDE_ASSIGN("/=", 144, OperandCount.BINARY, Associativity.RIGHT),
    MULTIPLY_ASSIGN("*=", 144, OperandCount.BINARY, Associativity.RIGHT),
    REMAINDER_ASSIGN("%=", 144, OperandCount.BINARY, Associativity.RIGHT),
    ADD_ASSIGN("+=", 144, OperandCount.BINARY, Associativity.RIGHT),
    SUBTRACT_ASSIGN("-=", 144, OperandCount.BINARY, Associativity.RIGHT),
    BITWISE_LEFT_MOVE_ASSIGN("<<=", 144, OperandCount.BINARY, Associativity.RIGHT),
    BITWISE_RIGHT_MOVE_ASSIGN(">>=", 144, OperandCount.BINARY, Associativity.RIGHT),
    BITWISE_AND_ASSIGN("&=", 144, OperandCount.BINARY, Associativity.RIGHT),
    BITWISE_XOR_ASSIGN("^=", 144, OperandCount.BINARY, Associativity.RIGHT),
    BITWISE_OR_ASSIGN("|=", 144, OperandCount.BINARY, Associativity.RIGHT),

    // 逗号运算符
    LAMBDA("->", 152, OperandCount.BINARY, Associativity.NONE),
    COMMA(",", 160, OperandCount.BINARY, Associativity.LEFT);

    // TODO ...More
    // 运算符的符号
    private final String name;
    // 优先级
    private final int priority;
    // 操作数
    private final OperandCount operandCount;
    // 结合性
    private final Associativity associativity;

    /**
     * @return {@link Keywords#OPERATOR_KEYWORD_MAP}
     */
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
