package org.harvey.compiler.syntax;

import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.test.version1.element.OperatorString;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-03 23:09
 */
public class OperatorPredicateForPreOperator implements OperatorPredicate {
    /**
     * 不得单右
     */
    private static final OperatorPredicate FOR_PRE_UNARY_LEFT = FOR_PRE_ITEM;
    /**
     * only 单右
     */
    private static final OperatorPredicate FOR_PRE_UNARY_RIGHT = FOR_PRE_NULL;
    /**
     * 不得单左
     */
    private static final OperatorPredicate FOR_PRE_BINARY = (associativity, operandCount) -> associativity !=
                                                                                             Associativity.LEFT ||
                                                                                             operandCount !=
                                                                                             OperandCount.UNARY;
    private final OperatorString pre;

    OperatorPredicateForPreOperator(OperatorString pre) {
        this.pre = pre;
    }

    @Override
    public boolean test(Associativity associativity, OperandCount operandCount) {
        // 前一个是Operator那这一个是啥啊...
        // 前一个是单左, 则next不能是单右
        // 前一个是双, 则next不能是单左
        // 前一个是单右, 则next必须是单右
        if (pre.getOperandCount() == OperandCount.BINARY) {
            return FOR_PRE_BINARY.test(associativity, operandCount);
        } else if (pre.getOperandCount() == OperandCount.UNARY) {
            if (pre.getAssociativity() == Associativity.LEFT) {
                return FOR_PRE_UNARY_LEFT.test(associativity, operandCount);
            } else if (pre.getAssociativity() == Associativity.RIGHT) {
                return FOR_PRE_UNARY_RIGHT.test(associativity, operandCount);
            } else {
                throw new CompilerException("Unknown associativity: " + pre.getAssociativity());
            }
        } else {
            throw new CompilerException("Unknown operand count: " + pre.getOperandCount());
        }
    }
}
