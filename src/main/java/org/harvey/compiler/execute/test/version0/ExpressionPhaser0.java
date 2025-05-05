package org.harvey.compiler.execute.test.version0;

import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.execute.expression.ConstantString;
import org.harvey.compiler.execute.expression.ConstantType;
import org.harvey.compiler.execute.expression.NormalOperatorString;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;
import org.harvey.compiler.io.source.SourcePosition;

import java.io.PrintStream;
import java.util.Stack;

/**
 * TODO
 * 分析表达式的工具, 将表达式转为简单的命令
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-25 18:36
 */
public class ExpressionPhaser0 {
    private static final PrintStream OUT = System.out;
    private final Stack<OperatorString> operators = new Stack<>();
    private final Stack<ItemString> items = new Stack<>();
    public int tempNameCreatorCounter = 0;


    private static Operator decide(String operName, Operator[] candidate) {
        if (candidate.length == 1) {
            return candidate[0];
        }

        return candidate[0];
    }

    public void addOperator(OperatorString operator) {
        deal(operator);
    }


    public void addItem(ItemString itemString) {
        items.push(itemString);
    }

    public void end() {
        while (!operators.empty()) {
            print(operators.pop());
        }
        assert items.size() == 1;
        printResult(items.pop());
    }

    private void printResult(ItemString result) {
        OUT.println("result = " + result.show());
    }

    public void phaseExpression(String[] strings) {
        for (String s : strings) {
            Operator[] candidate = Operators.get(s);
            if (candidate.length == 0) {
                // 不是operator, 就是identifier了
                items.push(
                        new ConstantString(SourcePosition.UNKNOWN, s.getBytes(), ConstantType.STRING));
            } else {
                deal(new NormalOperatorString(SourcePosition.UNKNOWN, decide(s, candidate)));
            }
        }
        while (!operators.empty()) {
            print(operators.pop());
        }
        if (items.size() == 1) {
            printResult(items.pop());
        } else if (items.size() > 1) {
            printResult(items.pop());
        }
    }

    private void deal(OperatorString operator) {
        while (true) {
            if (operators.empty()) {
                operators.push(operator);
                return;
            }
            OperatorString top = operators.peek();
            if (!operator.isPost()) {
                if (top.isPre() ||
                    operator.getPriority() < top.getPriority() ||
                    (operator.getPriority() == top.getPriority() &&
                     operator.getAssociativity() == Associativity.RIGHT)) {
                    operators.push(operator);
                    return;
                }
                print(operators.pop());
                continue;
            }
            OperatorString pair = operator.pair();
            while (!operators.empty()) {
                top = operators.pop();
                if (top.operatorEquals(pair)) {
                    return;
                }
                if (top.isPre()) {
                    throw new IllegalArgumentException("Need post part of " + top.show());
                }
                print(top);
            }
            return;
        }
    }

    private void print(OperatorString pop) {
        ItemString t;
        OperandCount count = pop.getOperandCount();
        switch (count) {
            case UNARY:
                ItemString unique = items.pop();
                t = newTempName();
                OUT.println(t.show() + " <- " + pop.show() + " " + unique.show());
                items.push(t);
                break;
            case BINARY:
                ItemString after = items.pop();
                ItemString before = items.pop();
                t = newTempName();
                OUT.println(t.show() + " <- " + before.show() + " " + pop.show() + " " + after.show());
                items.push(t);
                break;
            case MEANINGLESS:
                throw new IllegalArgumentException(count + "?");
        }
    }

    private ItemString newTempName() {
        return new ConstantString(SourcePosition.UNKNOWN, String.format("t%d", tempNameCreatorCounter++).getBytes(),
                ConstantType.STRING
        );
    }
}
