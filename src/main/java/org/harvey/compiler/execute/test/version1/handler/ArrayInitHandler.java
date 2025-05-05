package org.harvey.compiler.execute.test.version1.handler;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.test.version1.element.ArrayInitEachWarp;
import org.harvey.compiler.execute.test.version1.element.ArrayInitElement;
import org.harvey.compiler.execute.test.version1.element.ComplexExpressionWrap;
import org.harvey.compiler.execute.test.version1.env.ArrayElementOuter;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.execute.test.version1.pipeline.TodoTask;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * <pre>{@code
 * 思考, 把array init设计成什么样子
 * Array<int> arr = new Array<int>(100,0);
 * Array<int> arr = new Array<int>(100);
 * Array<int> arr = {1,2,3,4,null};
 * 可以
 * int a  = {1L,2,3,4,5, 12L}[2]; 不行
 * }</pre>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-06 21:07
 */
public class ArrayInitHandler implements ExpressionHandler {
    @Override
    public boolean handle(ExpressionContext context) {

        SourceString next = context.next();
        context.previousMove();
        if (next.getType() != SourceType.SIGN && !ArrayInitEachWarp.START.equals(next.getValue())) {
            return false;
        }
        SourceTextContext arrayInitSource = SourceTextContext.skipNest(context, ArrayInitEachWarp.START,
                ArrayInitEachWarp.END, true
        );
        SourcePosition start = arrayInitSource.removeFirst().getPosition();
        arrayInitSource.removeLast();
        ListIterator<SourceString> iterator = arrayInitSource.listIterator();
        ArrayElementOuter arrayElementOuter = new ArrayElementOuter(context.getEnvironment());
        List<ComplexExpressionWrap> warps = new ArrayList<>();
        int index = 0;
        while (iterator.hasNext()) {
            List<SourceString> eachElement = SourceTextContext.skipUntilComma(iterator, ArrayList::new);
            if (iterator.hasNext()) {
                if (!CollectionUtil.skipIf(iterator, s -> Operator.COMMA.nameEquals(s.getValue()))) {
                    throw new AnalysisExpressionException(
                            iterator.next().getPosition(), "expected , for array element");
                }
                if (!iterator.hasNext()) {
                    throw new AnalysisExpressionException(iterator.previous().getPosition(), "not except empty");
                }
            }
            if (eachElement.isEmpty()) {
                throw new AnalysisExpressionException(
                        iterator.previous().getPosition(), "except more for array element");
            }
            ArrayInitEachWarp argumentWrap = new ArrayInitEachWarp(eachElement.get(0).getPosition(), arrayElementOuter,
                    index
            );
            warps.add(argumentWrap);
            context.addTodoTask(new TodoTask(arrayElementOuter, eachElement, argumentWrap));
            index++;
        }
        ComplexExpressionWrap[] elementWarps = warps.toArray(new ComplexExpressionWrap[0]);
        arrayElementOuter.availableContext(context);
        context.add(new ArrayInitElement(start, elementWarps));
        return true;
    }
}
