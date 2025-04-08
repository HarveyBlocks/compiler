package org.harvey.compiler.execute.test.version0;

import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.io.source.SourceString;

import java.io.PrintStream;
import java.util.List;
import java.util.Stack;

/**
 * TODO
 * 分析表达式的工具, 将表达式转为简单的命令
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-25 18:36
 */
public class ExpressionPhaser {
    private static final PrintStream OUT = System.out;
    private final Stack<PhasingOperator> operators = new Stack<>();
    private final Stack<String> items = new Stack<>();
    public int tempNameCreatorCounter = 0;

    private static void printlnArray(Object... obj) {
        printArray(obj);
        OUT.println();
    }

    private static void printArray(Object... obj) {
        if (obj == null) {
            OUT.print("null");
            return;
        } else if (obj.length == 0) {
            OUT.print("[]");
            return;
        }
        OUT.print("[" + obj[0]);
        for (int i = 1; i < obj.length; i++) {
            OUT.print(" " + obj[i]);
        }
        OUT.print("]");
    }

    public void phaseExpression(List<SourceString> strings) {
        ExpressionPhaser.printlnArray(strings);
        for (SourceString s : strings) {
            switch (s.getType()) {
                case IDENTIFIER:
                case STRING:
                case BOOL:
                case CHAR:
                case SCIENTIFIC_NOTATION_FLOAT64:
                case SCIENTIFIC_NOTATION_FLOAT32:
                case INT32:
                case INT64:
                case FLOAT32:
                case FLOAT64:
                    items.push(s.getValue());
                    break;
                case OPERATOR:
                    deal(Operators.getByEnumName(s.getValue()));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Type");
            }
        }
        while (!operators.empty()) {
            print(operators.pop());
        }
        assert items.size() == 1;
        OUT.println("result = " + items.pop());
    }

    public void phaseExpression(String[] strings) {
        ExpressionPhaser.printlnArray((Object[]) strings);
        for (String s : strings) {
            Operator[] candidate = Operators.get(s);
            if (candidate.length == 0) {
                // 不是operator, 就是identifier了
                items.push(s);
            } else {
                deal(decide(s, candidate));
            }
        }
        while (!operators.empty()) {
            print(operators.pop());
        }
        if (items.size() == 1) {
            OUT.println("result = " + items.pop());
        } else if (items.size() > 1) {
            OUT.println("result = " + items.pop());
        }
    }

    private static Operator decide(String operName, Operator[] candidate) {
        if (candidate.length == 1) {
            return candidate[0];
        }

        return candidate[0];
    }


    private void deal(Operator operator) {
        if (operator == Operator.CALL_PRE) {
            operators.push(PhasingOperatorFactory.CALL);
        }
        while (true) {
            if (operators.empty()) {
                operators.push(PhasingOperatorFactory.normal(operator));
                return;
            }
            PhasingOperator top = operators.peek();
            if (!Operators.isPost(operator)) {
                if (top.isPre() ||
                    operator.getPriority() < top.getPriority() ||
                    (operator.getPriority() == top.getPriority() &&
                     operator.getAssociativity() == Associativity.RIGHT)) {
                    operators.push(PhasingOperatorFactory.normal(operator));
                    return;
                }
                print(operators.pop());
                continue;
            }
            PhasingOperator pair = PhasingOperatorFactory.normal(Operators.pair(operator));
            while (!operators.empty()) {
                top = operators.pop();
                if (top == pair) {
                    return;
                }
                if (top.isPre()) {
                    throw new IllegalArgumentException("Need post part of " + top.getName());
                }
                print(top);
            }
            return;
        }
    }

    private void print(PhasingOperator pop) {
        String t;
        OperandCount count = pop.getOperandCount();
        switch (count) {
            case UNARY:
                String unique = items.pop();
                t = newTempName();
                OUT.println(t + " <- " + pop.getName() + " " + unique);
                items.push(t);
                break;
            case BINARY:
                String after = items.pop();
                String before = items.pop();
                t = newTempName();
                OUT.println(t + " <- " + before + " " + pop.getName() + " " + after);
                items.push(t);
                break;
            case TERNARY:
            case NONE:
                throw new IllegalArgumentException(count + "?");
        }

    }

    private String newTempName() {
        return String.format("t%d", tempNameCreatorCounter++);
    }
}
