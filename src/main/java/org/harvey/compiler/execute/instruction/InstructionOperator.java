package org.harvey.compiler.execute.instruction;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-12 16:30
 */
@AllArgsConstructor
@Getter
public enum InstructionOperator {
    GOTO_LABEL(1),
    JUMP_OFFSET(1),
    //  transform type1 type2 num1 num2
    TRANSFORM(4),
    // 符号
    // positive type num
    POSITIVE(2),
    NEGATIVE(2),
    // 算术
    // add type num1 num2
    ADD(3),
    SUBTRACT(3),
    MULTIPLY(3),
    DIVIDE(3),
    REMAINDER(3),
    // 位运算
    BIT_XOR(3),
    BIT_OR(3),
    BIT_AND(3),
    BIT_NOT(3),
    LEFT_MOVE(3),
    RIGHT_MOVE(3),
    // 比较
    LARGER(3),
    LESS(3),
    LARGER_EQUALS(3),
    LESS_EQUALS(3),
    EQUALS(3),
    NOT_EQUALS(3),
    // 逻辑
    AND(2),
    OR(2),
    NOT(2),
    // new reference 类型引用ID 构造器引用ID
    NEW(2),
    // call id 需要知道栈中的那些个值需要被填入参数列表
    CALL_FUNCTION(2), // 调用函数
    // 还需要注入obj的this
    // call method id obj_reference 参数个数
    CALL_NONSTATIC_METHOD(3), // 调用方法
    ;


    private final int needArgCount;

    public int getCode() {
        return ordinal();
    }
}

enum TypeCode {
    REFERENCE,
    INT8,
    INT16,
    INT32,
    INT64,
    UINT8,
    UINT16,
    UINT32,
    UINT64,
    FLOAT32,
    FLOAT64,
    BOOL
}