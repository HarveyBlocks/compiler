package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.execute.test.version1.element.OperatorString;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 表达式中的运算符
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:46
 */
@Getter
public class NormalOperatorString extends ExpressionElement implements OperatorString {
    private final Operator value;

    public NormalOperatorString(SourcePosition sp, Operator value) {
        super(sp);
        this.value = value;
    }


    /**
     * @param element  null for false
     * @param operator
     * @return
     */
    public static boolean is(ExpressionElement element, Operator operator) {
        return element instanceof NormalOperatorString && ((NormalOperatorString) element).value == operator;
    }

    public String getName() {
        return value.getName();
    }

    @Override
    public int getPriority() {
        return value.getPriority();
    }

    @Override
    public Associativity getAssociativity() {
        return value.getAssociativity();
    }

    @Override
    public OperandCount getOperandCount() {
        return value.getOperandCount();
    }

    @Override
    public String show() {
        return this.value.getName();
    }

    @Override
    public boolean isPost() {
        return Operators.isPost(this.value);
    }

    @Override
    public boolean isPre() {
        return Operators.isPre(this.value);
    }

    @Override
    public OperatorString pair() {
        return new NormalOperatorString(this.getPosition(), Operators.pair(this.value));
    }

    @Override
    public boolean operatorEquals(OperatorString string) {
        return string instanceof NormalOperatorString && ((NormalOperatorString) string).value == this.value;
    }

    public boolean isOperator(Operator operator) {
        return value == operator;
    }

    @Override
    public String toString() {
        return value.name();
    }
}
