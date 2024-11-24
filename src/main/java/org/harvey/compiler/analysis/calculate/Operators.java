package org.harvey.compiler.analysis.calculate;

import org.harvey.compiler.common.util.CollectionUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 存储运算符表
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-15 21:04
 */
public class Operators {

    public static final List<Operator> TABLE;


    static {
        TABLE = List.of(Operator.values());
    }


    public static final Set<Operator> SET;

    static {
        SET = TABLE.stream().collect(Collectors.toUnmodifiableSet());
    }

    public static final Set<String> NAME_SET;

    static {
        NAME_SET = SET.stream().map(Operator::getName).collect(Collectors.toUnmodifiableSet());
    }

    public static final Set<Operator> ASSIGNS;

    static {
        ASSIGNS = Set.of(Operator.ASSIGN,
                Operator.DIVIDE_ASSIGN,
                Operator.MULTIPLY_ASSIGN,
                Operator.REMAINDER_ASSIGN,
                Operator.ADD_ASSIGN,
                Operator.SUBTRACT_ASSIGN,
                Operator.BITWISE_LEFT_MOVE_ASSIGN,
                Operator.BITWISE_IGNORE_SIGN_LEFT_MOVE_ASSIGN,
                Operator.BITWISE_RIGHT_MOVE_ASSIGN,
                Operator.BITWISE_IGNORE_SIGN_RIGHT_MOVE_ASSIGN,
                Operator.BITWISE_AND_ASSIGN,
                Operator.BITWISE_XOR_ASSIGN,
                Operator.BITWISE_OR_ASSIGN
        );
    }

    public static boolean isAssign(String value) {
        return CollectionUtil.contains(Operators.ASSIGNS, operator -> operator.nameEquals(value));
    }
}
