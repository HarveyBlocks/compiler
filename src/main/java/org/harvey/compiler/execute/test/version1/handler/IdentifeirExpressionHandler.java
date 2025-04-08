package org.harvey.compiler.execute.test.version1.handler;

import lombok.AllArgsConstructor;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.NormalOperatorString;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;

/**
 * 对Identifier的处理, 如果下一个是函数调用的括号, 那么会是{@link PossibleCallableSupplier}加入context
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:49
 */
@AllArgsConstructor
public class IdentifeirExpressionHandler implements ExpressionHandler {


    @Override
    public boolean handle(ExpressionContext context) {
        SourceString next = context.next();
        if (next.getType() != SourceType.IDENTIFIER) {
            context.previousMove();
            return false;
        }

        MemberSupplier supplier = getElement(context, next);
        context.add(supplier);
        return true;
    }

    /**
     * @param context not read only at the time of callable declare operator reload: must add () and invoke
     *                {@link #possibleCallableSupplierForOperatorReload(ExpressionContext, MemberSupplier, SourceString)}
     */
    private static MemberSupplier getElement(ExpressionContext context, SourceString identifier) {
        ExpressionElement expectPreGetMember = null;
        if (context.expressionHasPrevious()) {
            expectPreGetMember = context.getPrevious();
        }
        if (context.nextIs(ss -> Operator.PARENTHESES_PRE.nameEquals(ss.getValue()))) {
            // 是函数调用
            // get previous, 并
            return forCallable(context, identifier, expectPreGetMember);
        } else {
            if (expectPreGetMember == null) {
                // 第一个
                return forFirstIdentifier(context, identifier);
            } else {
                MemberType memberType = forMember(context, expectPreGetMember);
                return memberType.find(identifier.getPosition(), identifier.getValue());
            }
        }
    }

    private static PossibleCallableSupplier forCallable(
            ExpressionContext context,
            SourceString identifier,
            ExpressionElement expectPreGetMember) {
        if (expectPreGetMember == null) {
            // 第一个
            PossibleCallableSupplier callableSupplier = context.getEnvironment()
                    .getMemberManager()
                    .createPossibleCallable(
                            identifier.getPosition(), identifier.getValue());
            if (callableSupplier != null) {
                return callableSupplier;
            }
            // 没找到... 是重载
            MemberSupplier memberSupplier = forFirstIdentifier(context, identifier);
            return possibleCallableSupplierForOperatorReload(context, memberSupplier, identifier);
        } else {
            MemberType previousMemberType = forMember(context, expectPreGetMember);
            PossibleCallableSupplier possibleCallable = previousMemberType.findPossibleCallable(
                    identifier.getPosition(), identifier.getValue());
            if (possibleCallable != null) {
                return possibleCallable;
            }
            MemberSupplier memberSupplier = previousMemberType.find(
                    identifier.getPosition(), identifier.getValue());
            return possibleCallableSupplierForOperatorReload(context, memberSupplier, identifier);
        }
    }

    /**
     * @param context read only
     */
    private static MemberType forMember(
            ExpressionContext context, ExpressionElement expectPreGetMember) {
        ExpressionElement pre = null;
        if (NormalOperatorString.is(expectPreGetMember, Operator.GET_MEMBER)) {
            if (!context.expressionHasPrevious()) {
                throw new CompilerException("must something before `get member`");
            }
            // excepted member supplier
            pre = context.getPrevious();
        }
        if (!(pre instanceof MemberSupplier)) {
            throw new CompilerException("excepted member supplier");
        }
        return ((MemberSupplier) pre).getType();
    }

    /**
     * @param context read only
     */
    private static MemberSupplier forFirstIdentifier(ExpressionContext context, SourceString identifier) {
        MemberSupplier memberSupplier = context.getEnvironment().getMemberManager().createPossibleCallable(
                identifier.getPosition(), identifier.getValue());
        if (memberSupplier == null) {
            // 没找到...
            throw new AnalysisExpressionException(identifier.getPosition(), "unknown identifier");
        }
        return memberSupplier;
    }


    /**
     * <pre>{@code
     *  context.add(memberSupplier);
     *  context.add(new NormalOperatorString(identifier.getPosition(),Operator.GET_MEMBER));
     *  // 下面的操作移到外界
     *  // context.add(callableSupplier);
     *  // context.add(new CompileOperatorString(identifier.getPosition(), CompileOperatorString.CompileOperator.INVOKE));
     * }</pre>
     *
     * @param memberSupplier provide callable declare
     */
    private static PossibleCallableSupplier possibleCallableSupplierForOperatorReload(
            ExpressionContext context, MemberSupplier memberSupplier, SourceString identifier) {

        MemberType memberType = memberSupplier.getType();
        PossibleCallableSupplier callableSupplier = memberType.find(
                identifier.getPosition(), Operator.CALLABLE_DECLARE);
        if (callableSupplier == null) {
            throw new AnalysisExpressionException(identifier.getPosition(), "unknown identifier");
        }
        // 是callable declare 的 reload
        context.add(memberSupplier);
        context.add(new NormalOperatorString(identifier.getPosition(), Operator.GET_MEMBER));
        return callableSupplier;
    }
}
