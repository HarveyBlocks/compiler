package org.harvey.compiler.syntax;

import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;

import java.util.function.BiPredicate;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-03 23:09
 */
@FunctionalInterface
public interface OperatorPredicate extends BiPredicate<Associativity, OperandCount> {
    /**
     * only 单右
     */
    OperatorPredicate FOR_PRE_NULL = (associativity, operandCount) -> associativity == Associativity.RIGHT &&
                                                                      operandCount == OperandCount.UNARY;
    /**
     * 不得单右
     */
    OperatorPredicate FOR_PRE_ITEM = (associativity, operandCount) -> associativity != Associativity.RIGHT ||
                                                                      operandCount != OperandCount.UNARY;

    @Override
    boolean test(Associativity associativity, OperandCount operandCount);


}
