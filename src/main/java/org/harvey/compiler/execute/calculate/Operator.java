package org.harvey.compiler.execute.calculate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.common.util.Counter;
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
    CALLABLE_DECLARE("()", Priority.next(), OperandCount.MEANINGLESS, Associativity.LEFT), // 重载
    ARRAY_DECLARE("[]", Priority.stay(), OperandCount.MEANINGLESS, Associativity.LEFT), // 重载
    MULTIPLE_TYPE("...", Priority.next(), OperandCount.UNARY, Associativity.RIGHT), // 不定参数
    // 
    GENERIC_LIST_PRE("[", Priority.next(), OperandCount.MEANINGLESS, Associativity.LEFT), // 声明泛型, 前缀
    GENERIC_LIST_POST("]", Priority.stay(), OperandCount.MEANINGLESS, Associativity.LEFT), // 声明泛型, 前缀

    CALL_PRE("(", Priority.stay(), OperandCount.MEANINGLESS, Associativity.LEFT), // 函数调用前缀
    CALL_POST(")", Priority.stay(), OperandCount.MEANINGLESS, Associativity.LEFT),  // 函数调用后缀
    ARRAY_AT_PRE("[", Priority.next(), OperandCount.MEANINGLESS, Associativity.LEFT), // 取索引, 前缀
    ARRAY_AT_POST("]", Priority.stay(), OperandCount.MEANINGLESS, Associativity.LEFT), // 取索引, 后缀
    PARENTHESES_PRE("(", Priority.stay(), OperandCount.MEANINGLESS, Associativity.LEFT), // 括号前缀
    PARENTHESES_POST(")", Priority.stay(), OperandCount.MEANINGLESS, Associativity.LEFT),  // 括号后缀
    GET_MEMBER(
            String.valueOf(SourceFileConstant.GET_MEMBER), Priority.next(), OperandCount.BINARY,
            Associativity.LEFT
    ), // 成员调用

    // 符号
    NEGATIVE("-", Priority.next(), OperandCount.UNARY, Associativity.RIGHT), // 负
    POSITIVE("+", Priority.stay(), OperandCount.UNARY, Associativity.RIGHT), // 正
    LEFT_INCREASING("++", Priority.stay(), OperandCount.UNARY, Associativity.RIGHT), // 左自增
    RIGHT_INCREASING("++", Priority.stay(), OperandCount.UNARY, Associativity.LEFT), // 右自增 a ++ ++ ++
    LEFT_DECREASING("--", Priority.stay(), OperandCount.UNARY, Associativity.RIGHT),  // 左自减
    RIGHT_DECREASING("--", Priority.stay(), OperandCount.UNARY, Associativity.LEFT), // 右自减
    NOT("!", Priority.stay(), OperandCount.UNARY, Associativity.RIGHT), // 逻辑非
    BITWISE_NEGATION("~", Priority.stay(), OperandCount.UNARY, Associativity.RIGHT), // 按位取反

    // 乘除取余
    DIVIDE("/", Priority.next(), OperandCount.BINARY, Associativity.LEFT), // 除
    MULTIPLY("*", Priority.stay(), OperandCount.BINARY, Associativity.LEFT), // 乘
    REMAINDER("%", Priority.stay(), OperandCount.BINARY, Associativity.LEFT), // 取余
    ADD("+", Priority.next(), OperandCount.BINARY, Associativity.LEFT), // 加
    SUBTRACT("-", Priority.stay(), OperandCount.BINARY, Associativity.LEFT), // 减法
    BITWISE_LEFT_MOVE("<<", Priority.next(), OperandCount.BINARY, Associativity.LEFT),  // 位左移
    BITWISE_RIGHT_MOVE(">>", Priority.stay(), OperandCount.BINARY, Associativity.LEFT),  // 位右移


    // 比较
    LARGER(">", Priority.next(), OperandCount.BINARY, Associativity.LEFT),
    LARGER_EQUALS(">=", Priority.stay(), OperandCount.BINARY, Associativity.LEFT),
    LESS("<", Priority.stay(), OperandCount.BINARY, Associativity.LEFT),
    LESS_EQUALS("<=", Priority.stay(), OperandCount.BINARY, Associativity.LEFT),
    // 偏序关系 obj is Type
    IS("is", Priority.next(), OperandCount.BINARY, Associativity.LEFT), // a is b is c
    IN("in", Priority.next(), OperandCount.BINARY, Associativity.RIGHT), // 'a' is 'b' collectionIn 'c'
    // 等价关系
    EQUALS("==", Priority.next(), OperandCount.BINARY, Associativity.LEFT),
    NOT_EQUALS("!=", Priority.stay(), OperandCount.BINARY, Associativity.LEFT),

    // 位操作
    BITWISE_AND("&", Priority.next(), OperandCount.BINARY, Associativity.LEFT),
    BITWISE_XOR("^", Priority.next(), OperandCount.BINARY, Associativity.LEFT),
    BITWISE_OR("|", Priority.next(), OperandCount.BINARY, Associativity.LEFT),
    // 逻辑表达式
    AND("&&", Priority.next(), OperandCount.BINARY, Associativity.LEFT),
    OR("||", Priority.next(), OperandCount.BINARY, Associativity.LEFT),

    // 条件运算符
    CONDITION_CHECK("?", Priority.next(), OperandCount.BINARY, Associativity.LEFT),
    CONDITION_DECIDE(":", Priority.stay(), OperandCount.BINARY, Associativity.LEFT),

    // 赋值和复合赋值
    ASSIGN("=", Priority.next(), OperandCount.BINARY, Associativity.RIGHT),
    DIVIDE_ASSIGN("/=", Priority.stay(), OperandCount.BINARY, Associativity.RIGHT),
    MULTIPLY_ASSIGN("*=", Priority.stay(), OperandCount.BINARY, Associativity.RIGHT),
    REMAINDER_ASSIGN("%=", Priority.stay(), OperandCount.BINARY, Associativity.RIGHT),
    ADD_ASSIGN("+=", Priority.stay(), OperandCount.BINARY, Associativity.RIGHT),
    SUBTRACT_ASSIGN("-=", Priority.stay(), OperandCount.BINARY, Associativity.RIGHT),
    BITWISE_LEFT_MOVE_ASSIGN("<<=", Priority.stay(), OperandCount.BINARY, Associativity.RIGHT),
    BITWISE_RIGHT_MOVE_ASSIGN(">>=", Priority.stay(), OperandCount.BINARY, Associativity.RIGHT),
    BITWISE_AND_ASSIGN("&=", Priority.stay(), OperandCount.BINARY, Associativity.RIGHT),
    BITWISE_XOR_ASSIGN("^=", Priority.stay(), OperandCount.BINARY, Associativity.RIGHT),
    BITWISE_OR_ASSIGN("|=", Priority.stay(), OperandCount.BINARY, Associativity.RIGHT),

    // lambda
    LAMBDA("->", Priority.next(), OperandCount.BINARY, Associativity.RIGHT),
    // 逗号运算符
    COMMA(",", Priority.next(), OperandCount.BINARY, Associativity.LEFT);

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

    private static class Priority {
        static final Counter COUNTER = new Counter(0, 1);

        static int next() {
            return COUNTER.next();
        }

        static int stay() {
            return COUNTER.gePrevious();
        }
    }
}
