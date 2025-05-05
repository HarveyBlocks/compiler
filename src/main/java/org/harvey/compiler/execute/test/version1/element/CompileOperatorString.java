package org.harvey.compiler.execute.test.version1.element;

import lombok.Getter;
import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.IExpressionElement;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 有调用和类型转换的特殊类型, 不能再源码里写, 只能给编译器使用的, 有别于{@link Operator}的特殊Operator
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-04 17:04
 */
public class CompileOperatorString extends ExpressionElement implements OperatorString {
    private final CompileOperator operator;

    public CompileOperatorString(SourcePosition position, CompileOperator operator) {
        super(position);
        this.operator = operator;
    }

    public static boolean is(IExpressionElement element, CompileOperator compileOperator) {
        if (!(element instanceof CompileOperatorString)) {
            return false;
        }
        return ((CompileOperatorString) element).operator == compileOperator;
    }

    @Override
    public int getPriority() {
        return operator.getPriority();
    }

    @Override
    public Associativity getAssociativity() {
        return operator.getAssociativity();
    }

    @Override
    public OperandCount getOperandCount() {
        return operator.getOperandCount();
    }

    @Override
    public String show() {
        return this.operator.name();
    }

    @Override
    public boolean isPost() {
        return false;
    }

    @Override
    public boolean isPre() {
        return false;
    }

    @Override
    public OperatorString pair() {
        return null;
    }

    @Override
    public boolean operatorEquals(OperatorString string) {
        return string instanceof CompileOperatorString && ((CompileOperatorString) string).operator == this.operator;
    }

    @Override
    public String toString() {
        return operator.name();
    }

    @Getter
    public enum CompileOperator {
        // 函数名 invoke 参数列表
        INVOKE(Operator.RIGHT_INCREASING.getPriority(), Associativity.LEFT, OperandCount.BINARY), // 类型 cast 实例对象
        ARRAY_AT(Operator.RIGHT_INCREASING.getPriority(), Associativity.LEFT, OperandCount.BINARY), // 类型 cast 实例对象
        CAST(Operator.POSITIVE.getPriority(), Associativity.RIGHT, OperandCount.UNARY);

        private final int priority;
        private final Associativity associativity;
        private final OperandCount operandCount;

        CompileOperator(int priority, Associativity associativity, OperandCount operandCount) {
            this.priority = priority;
            this.associativity = associativity;
            this.operandCount = operandCount;
        }
    }
}
