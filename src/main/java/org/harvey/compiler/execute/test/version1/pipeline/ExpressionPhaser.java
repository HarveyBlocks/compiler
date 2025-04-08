package org.harvey.compiler.execute.test.version1.pipeline;

import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.test.version1.element.ComplexExpressionWrap;
import org.harvey.compiler.execute.test.version1.element.DefaultComplexExpressionWrap;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;
import org.harvey.compiler.execute.test.version1.handler.ExpressionHandler;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 全集 = unknown + local_variable + 非 local_variable
 * typeExceptGeneric 是 非Local_variable的子集
 * generic 是 非Local_variable的子集
 * parameterized type 只能再 ()中作为cast出现
 * parameterized type只能作为new的类型出现
 * new + parameterized type/new()
 * parameterized type只能在[]中作为参数类型出现
 * 当[被认定为generic list
 *
 * @date 2025-04-04 16:31
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class ExpressionPhaser {

    public ExpressionPhaser() {
    }

    List<ExpressionHandler> expressionHandlers = new ArrayList<>();

    public ExpressionPhaser register(ExpressionHandler expressionHandler) {
        expressionHandlers.add(expressionHandler);
        return this;
    }

    public Expression phase(List<SourceString> source, OuterEnvironment outerEnvironment) {
        ExpressionContext context = new ExpressionContext(source, outerEnvironment);
        return phase0(context);
    }

    private Expression phase0(ExpressionContext context) {
        Stack<Pair<ComplexExpressionWrap, ExpressionContext>> stack = new Stack<>();
        ComplexExpressionWrap root = new DefaultComplexExpressionWrap(SourcePosition.UNKNOWN){};
        stack.push(new Pair<>(root, context));
        while (!stack.empty()) {
            Pair<ComplexExpressionWrap, ExpressionContext> top = stack.peek();
            ExpressionContext topContext = top.getValue();
            if (topContext.hasNext()) {
                phaseOneLoop(topContext);
                while (topContext.hasTodo()) {
                    TodoTask todoTask = topContext.popTodoTask();
                    ExpressionContext newContext = new ExpressionContext(
                            todoTask.getTodoSource(),
                            todoTask.getOuterEnvironment()
                    );
                    stack.push(new Pair<>(todoTask.getWrap(), newContext));
                }
            }
            if (!topContext.hasNext()) {
                top.getKey().setExpression(topContext.result());
                stack.pop();
            }
        }
        return root.getExpression();
    }

    private void phaseOneLoop(ExpressionContext topContext) {
        boolean deal = false;
        for (ExpressionHandler expressionHandler : expressionHandlers) {
            if (expressionHandler.handle(topContext)) {
                deal = true;
                break;
            }
        }
        if (!deal) {
            SourceString next = topContext.next();
            throw new AnalysisExpressionException(next.getPosition(), "can not deal");
        }
    }

    @Deprecated
    private void recursivelyPhase0(ExpressionContext context) {
        while (context.hasNext()) {
            phaseOneLoop(context);
            while (context.hasTodo()) {
                TodoTask todoTask = context.popTodoTask();
                // 嵌套递归结构, 很不好
                // int a(int i){return i+1;};
                // print(a(a(a(a(a(a(1)))))));
                ExpressionContext newContext = new ExpressionContext(
                        todoTask.getTodoSource(), todoTask.getOuterEnvironment());
                recursivelyPhase0(newContext);
                todoTask.getWrap().setExpression(newContext.result());
            }
        }
    }

}
