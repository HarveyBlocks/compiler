package org.harvey.compiler.execute.test.version1.element;


import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.expression.IExpressionElement;

/**
 * 表达式
 * exp -> item
 * exp -> exp bi_oper exp
 * exp -> un_oper exp
 * exp -> (exp)
 * 这个接口就是上面的oper
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 20:24
 */
public interface OperatorString extends IExpressionElement {
    /**
     * 优先级值越大, 优先级越低
     *
     * @return a<b
     */
    static boolean priorityLower(OperatorString a, OperatorString b) {
        return a.getPriority() > b.getPriority();
    }

    int getPriority();


    Associativity getAssociativity();

    OperandCount getOperandCount();


    boolean isPost();

    boolean isPre();

    OperatorString pair();

    boolean operatorEquals(OperatorString string);
}
