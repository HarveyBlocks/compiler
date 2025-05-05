package org.harvey.compiler.execute.test.version1.handler;

import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.NormalOperatorString;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-06 23:58
 */
public class NormalOperatorHandler implements ExpressionHandler {
    private static Operator decide(ExpressionElement previous, Operator sign, Operator calculate) {
        if (previous instanceof ItemString) {
            return sign;
        }
        if (!(previous instanceof OperatorString)) {
            return calculate;
        }
        OperandCount operandCount = ((OperatorString) previous).getOperandCount();
        Associativity associativity = ((OperatorString) previous).getAssociativity();
        if (associativity == Associativity.RIGHT || operandCount == OperandCount.UNARY) {
            return sign;
        }
        return calculate;
    }

    @Override
    public boolean handle(ExpressionContext context) {
        SourceString exceptedOperator = context.next();
        Operator[] operators = Operators.get(exceptedOperator.getValue());
        if (operators == null || operators.length == 0) {
            context.previous();
            return false;
        }
        SourcePosition position = exceptedOperator.getPosition();
        if (operators.length == 1) {
            context.add(new NormalOperatorString(position, operators[0]));
            return true;
        }
        // TODO && || ?:
        // 优先级
        ExpressionElement previous = context.hasPrevious() ? null : context.getPrevious();
        context.add(new NormalOperatorString(position, distinguish(exceptedOperator.getValue(), previous)));
        return true;
    }

    private Operator distinguish(
            String name, ExpressionElement previous) {
        switch (name) {
            case "+":  // 正 or 加
                return decide(previous, Operator.POSITIVE, Operator.ADD);
            case "-":  // 负 or 减
                return decide(previous, Operator.NEGATIVE, Operator.SUBTRACT);
            case "++": // 左++ or 右++
                return decideLr(previous, Operator.LEFT_INCREASING, Operator.RIGHT_INCREASING);
            case "--": // 左-- or 右--
                return decideLr(previous, Operator.LEFT_DECREASING, Operator.RIGHT_DECREASING);
            case "[":  // generic or array at
                // 其实这个应该马上处理了的, 要么嵌套, 要么stack了
                return Operator.ARRAY_AT_PRE;
            case "]":  // generic or array at
                return Operator.ARRAY_AT_POST;
            case "(":  // call or PARENTHESES
                return Operator.PARENTHESES_PRE;
            case ")":  // call or PARENTHESES
                return Operator.PARENTHESES_POST;
            default:
                throw new CompilerException("Unknown operator name");
        }
    }

    private Operator decideLr(ExpressionElement previous, Operator left, Operator right) {
        if (previous instanceof ItemString) {
            // a++ - 2
            // - - 2
            return right;
        }
        // ++++a 报错
        return left;
    }
}
