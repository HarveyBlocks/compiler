package org.harvey.compiler.execute.test.version1.handler;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.NormalOperatorString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.using.ParameterizedType;

/**
 * 处理{@link Operator#GET_MEMBER}, 特别地, 判断下一个如果是Operator, 那么这个Operator应该是重载运算符
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:49
 */
@SuppressWarnings("unused")
public class GetMemberExpressionHandler implements ExpressionHandler {
    private static void tryReloadOperation(
            ExpressionContext context,
            SourceString operatorItem,
            MemberType memberBefore,
            SourcePosition position) {
        if (Operator.CALL_PRE.nameEquals(operatorItem.getValue())) {
            SourceString item = context.next();
            if (Operator.CALL_POST.nameEquals(item.getValue())) {
                PossibleCallableSupplier callableRelatedDeclares = memberBefore.find(
                        position,
                        Operator.CALLABLE_DECLARE
                );
                context.add(callableRelatedDeclares);
            } else {
                PossibleCallableSupplier callableRelatedDeclares = tryCast(context, memberBefore, position);
                item = context.next();
                if (!Operator.CALL_POST.nameEquals(item.getValue())) {
                    throw new AnalysisExpressionException(position, "expected: " + Operator.CALL_PRE);
                }
                /*类型信息*/
                context.add(callableRelatedDeclares);
            }
        } else if (Operator.ARRAY_AT_PRE.nameEquals(operatorItem.getValue())) {
            SourceString item = context.next();
            if (Operator.ARRAY_AT_POST.nameEquals(item.getValue())) {
                PossibleCallableSupplier callableRelatedDeclares = memberBefore.find(position, Operator.ARRAY_DECLARE);
                context.add(callableRelatedDeclares);
            } else {
                throw new AnalysisExpressionException(position, "expected: " + Operator.ARRAY_AT_POST);
            }
        } else {
            Operator[] operators = Operators.reloadableOperator(operatorItem.getValue());
            if (operators != null) {
                PossibleCallableSupplier callableRelatedDeclares = memberBefore.find(position, operators);
                context.add(callableRelatedDeclares);
            } else {
                throw new AnalysisExpressionException(position, "can not reload! ");
            }
        }
    }

    /**
     * 可能是重载了类型转换运算符
     *
     * @param context
     * @param memberBefore
     * @param position
     * @return
     */
    private static PossibleCallableSupplier tryCast(
            ExpressionContext context, MemberType memberBefore, SourcePosition position) {
        // 可能是cast
        ParameterizedType<ReferenceElement> parameterizedType = GenericFactory.parameterizedType(
                context,
                context.getEnvironment().getIdentifierManager()
        );
        MemberType relateParameterizedType = context.getEnvironment()
                .relateParameterizedType(parameterizedType);
        return memberBefore.findCastOperator(position, relateParameterizedType);
    }

    @Override
    public boolean handle(ExpressionContext context) {
        int a = 0;
        SourceString next = context.next();
        if (!Operator.GET_MEMBER.nameEquals(next.getValue())) {
            context.previousMove();
            return false;
        }
        ExpressionElement previous = null;
        if (context.expressionHasPrevious()) {
            previous = context.getPrevious();
        }


        if (!(previous instanceof MemberSupplier)) {
            throw new AnalysisExpressionException(
                    next.getPosition(),
                    "can not get any member for no member supplier previous"
            );
        }
        MemberType memberBefore = ((MemberSupplier) previous).getType();
        context.add(new NormalOperatorString(next.getPosition(), Operator.GET_MEMBER));
        // 检查后面的重载运算符
        SourceString item = context.next();
        SourcePosition position = item.getPosition();
        if (Operators.get(item.getValue()) == null) {
            context.previousMove();
            return true;
        }
        // 是重载运算符
        tryReloadOperation(context, item, memberBefore, position);
        return true;
    }
}
