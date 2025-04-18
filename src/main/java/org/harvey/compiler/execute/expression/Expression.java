package org.harvey.compiler.execute.expression;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;

/**
 * 表达式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:51
 */
public class Expression extends ArrayList<ExpressionElement> {
    // TODO NOT GOOD, 应该继承Expression类, 且不可读不可写
    public static final Expression EMPTY = new Expression();

    public Expression(Collection<? extends ExpressionElement> c) {
        super(c);
    }

    public Expression() {
        super();
    }

    public Expression(int initialCapacity) {
        super(initialCapacity);
    }

    public static Expression of(ComplexExpressionElement... element) {
        if (element == null) {
            return null;
        }
        Expression expression = new Expression();
        Collections.addAll(expression, element);
        return expression;
    }

    private static int checkBody(ExpressionElement element, int inBody, int inGeneric, int inCall, int inParentheses) {
        if (inGeneric != 0 || inCall != 0 || inParentheses != 0) {
            return inBody;
        }
        ComplexExpression ce = ((ComplexExpressionElement) element).getExpression();
        if (ce instanceof ArrayInitExpression) {
            if (((ArrayInitExpression) ce).isStart()) {
                inBody++;
            } else {
                inBody--;
            }
        } else if (ce instanceof StructCloneExpression) {
            if (((StructCloneExpression) ce).isStart()) {
                inBody++;
            } else {
                inBody--;
            }
        }
        // lambda不用担心
        if (inBody < 0) {
            throw new AnalysisExpressionException(element.getPosition(), "Illegal match");
        }
        return inBody;
    }

    public ArrayList<Expression> splitWithComma() {
        return splitWithComma((e, p) -> e);
    }

    public <T> ArrayList<T> splitWithComma(PartCreator<T> mapper) {
        // 本type 为同, 以, 分割的多个
        // int a = 2, c = 3, d , e = 4;
        // assign: identifier | identifier = using | identifier = using, assign
        // Declare = type DeclareInit
        // 对assign进行操作
        ArrayList<T> result = new ArrayList<>();
        // 要同一层的, 很困难, 有调用(),有括号(), 有<>, 有{}
        int inGeneric = 0;
        int inCall = 0;
        int inParentheses = 0;
        int inBody = 0;
        Expression part = new Expression();
        for (ExpressionElement element : this) {
            part.add(element);
            boolean isOper = element instanceof NormalOperatorString;
            boolean isBody = element instanceof ComplexExpressionElement;
            if (!isOper && !isBody) {
                continue;
            }
            if (isBody) {
                inBody = checkBody(element, inBody, inGeneric, inCall, inParentheses);
                continue;
            }
            switch (((NormalOperatorString) element).getValue()) {
                case COMMA:
                    if (inGeneric == 0 && inCall == 0 && inParentheses == 0 && inBody == 0) {
                        part.remove(part.size() - 1);// 删去,
                        result.add(mapper.apply(part, element.getPosition()));
                        part = new Expression();
                        continue;
                    }
                    break;
                case GENERIC_LIST_PRE:
                    if (inCall == 0 && inParentheses == 0 && inBody == 0) {
                        inGeneric++;
                    }
                    break;
                case GENERIC_LIST_POST:
                    if (inCall == 0 && inParentheses == 0 && inBody == 0) {
                        inGeneric--;
                    }
                    break;
                case CALL_PRE:
                    if (inGeneric == 0 && inParentheses == 0 && inBody == 0) {
                        inCall++;
                    }
                    break;
                case CALL_POST:
                    if (inGeneric == 0 && inParentheses == 0 && inBody == 0) {
                        inCall--;
                    }
                    break;
                case PARENTHESES_PRE:
                    if (inGeneric == 0 && inCall == 0 && inBody == 0) {
                        inParentheses++;
                    }
                    break;
                case PARENTHESES_POST:
                    if (inGeneric == 0 && inCall == 0 && inBody == 0) {
                        inParentheses--;
                    }
                    break;
            }
            if (inGeneric < 0 || inCall < 0 || inParentheses < 0 || inBody < 0) {
                throw new AnalysisExpressionException(element.getPosition(), "Illegal match");
            }

        }
        if (!part.isEmpty()) {
            if (inGeneric != 0 || inCall != 0 || inParentheses != 0 || inBody != 0) {
                throw new AnalysisExpressionException(part.get(part.size() - 1).getPosition(), "Illegal match");
            }
            result.add(mapper.apply(part, SourcePosition.UNKNOWN));
        }
        return result;
    }

    @FunctionalInterface
    public interface PartCreator<R> extends BiFunction<Expression, SourcePosition, R> {
    }

}
