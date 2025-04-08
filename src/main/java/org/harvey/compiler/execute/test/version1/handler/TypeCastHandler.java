package org.harvey.compiler.execute.test.version1.handler;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.element.CastParameterizedType;
import org.harvey.compiler.execute.test.version1.element.CompileOperatorString;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.using.ParameterizedType;

/**
 * 类型转换的Operator的转换{@link CompileOperatorString.CompileOperator#CAST}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 22:32
 */
public class TypeCastHandler implements ExpressionHandler {
    @Override
    public boolean handle(ExpressionContext context) {
        // 是 类型 吗?
        SourceString expectParenthesesPre = context.next();
        if (!Operator.PARENTHESES_PRE.nameEquals(expectParenthesesPre.getValue())) {
            context.previousMove();
            return false;
        }
        SourcePosition position = expectParenthesesPre.getPosition();
        MemberType castTarget;
        context.mark();
        try {
            ParameterizedType<ReferenceElement> type = GenericFactory.parameterizedType(
                    context, context.getEnvironment().getIdentifierManager());
            castTarget = context.getEnvironment().relateParameterizedType(type);
        } catch (Exception e) {
            // 失败
            context.returnToMark();
            return false;
        } finally {
            context.removeMark();
        }
        /*类型信息*/
        context.add(new CastParameterizedType(position, castTarget));
        SourceString expectParenthesesPost = context.next();
        position = expectParenthesesPost.getPosition();
        if (!Operator.PARENTHESES_POST.nameEquals(expectParenthesesPost.getValue())) {
            throw new AnalysisExpressionException(position, "expect parentheses post");
        }
        /*一个operator, 是cast*/
        context.add(new CompileOperatorString(castTarget.getPosition(), CompileOperatorString.CompileOperator.CAST));
        return true;
    }
}
