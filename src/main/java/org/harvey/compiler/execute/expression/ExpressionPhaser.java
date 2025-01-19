package org.harvey.compiler.execute.expression;

import org.harvey.compiler.analysis.calculate.Associativity;
import org.harvey.compiler.analysis.calculate.OperandCount;
import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.calculate.Operators;
import org.harvey.compiler.io.source.SourceString;

import java.io.PrintStream;
import java.util.List;
import java.util.Stack;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-25 18:36
 */
public class ExpressionPhaser {
    private static final PrintStream OUT = System.out;


    private final Stack<Operator> operators = new Stack<>();
    private final Stack<String> items = new Stack<>();
    public int counter = 0;

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
                items.push(s);
            } else {
                deal(candidate[0]);
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

    private void deal(Operator operator) {
        while (true) {
            if (operators.empty()) {
                operators.push(operator);
                return;
            }
            Operator top = operators.peek();
            if (!Operators.isPost(operator)) {
                if (Operators.isPre(top) || operator.getPriority() < top.getPriority() ||
                        (operator.getPriority() == top.getPriority() &&
                                operator.getAssociativity() == Associativity.RIGHT)) {
                    operators.push(operator);
                    return;
                }
                print(operators.pop());
                continue;
            }
            Operator pair = Operators.pair(operator);
            while (!operators.empty()) {
                top = operators.pop();
                if (top == pair) {
                    return;
                }
                if (Operators.isPre(top)) {
                    throw new IllegalArgumentException("Need post part of " + top.getName());
                }
                print(top);
            }
            return;
        }
    }

    private void print(Operator pop) {
        String t;
        OperandCount count = pop.getOperandCount();
        switch (count) {
            case UNARY:
                String unique = items.pop();
                t = "t" + (counter++);
                OUT.println(t + " <- " + pop.getName() + " " + unique);
                items.push(t);
                break;
            case BINARY:
                String after = items.pop();
                String before = items.pop();
                t = "t" + (counter++);
                OUT.println(t + " <- " + before + " " + pop.getName() + " " + after);
                items.push(t);
                break;
            case TERNARY:
            case NONE:
                throw new IllegalArgumentException(count + "?");
        }

    }
}
