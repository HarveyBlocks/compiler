package org.harvey.compiler.execute.test.version1.handler;

import lombok.AllArgsConstructor;
import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.exception.self.UnfinishedException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.NormalOperatorString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.manager.MemberManager;
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
public class IdentifierExpressionHandler implements ExpressionHandler {


    /**
     * @param context not read only at the time of callable declare operator reload: must addIdentifier () and invoke
     *                {@link #possibleCallableSupplierForOperatorReload(ExpressionContext, MemberSupplier, SourceString)}
     */
    private static MemberSupplier getElement(ExpressionContext context, SourceString identifier) {
        ExpressionElement expectPreGetMember = null;
        if (context.expressionHasPrevious()) {
            expectPreGetMember = context.getPrevious();
        }
        MemberType memberType = null;
        if (NormalOperatorString.is(expectPreGetMember, Operator.GET_MEMBER)) {
            if (!context.expressionHasPrevious()) {
                throw new CompilerException("must something before `get member`");
            }
            // excepted member supplier
            ExpressionElement pre = context.getPrevious();
            if (!(pre instanceof MemberSupplier)) {
                throw new CompilerException("excepted member supplier");
            }
            memberType = ((MemberSupplier) pre).getType();
        }
        if (context.nextIs(ss -> Operator.PARENTHESES_PRE.nameEquals(ss.getValue()))) {
            // 是函数调用
            // get previous, 并
            return forCallable(context, identifier, memberType);
        } else {
            if (memberType == null) {
                // 第一个
                return forFirstIdentifier(context, identifier);
            } else {
                return memberType.find(identifier.getPosition(), identifier.getValue());
            }
        }
    }

    private static PossibleCallableSupplier forCallable(
            ExpressionContext context, SourceString identifier, MemberType memberType) {
        if (memberType == null) {
            // 第一个
            // 第一个是static的path, 就是org.这种, 或者 import table 这种
            MemberManager memberManager = context.getEnvironment().getMemberManager();
            // 局部变量->false
            // 能从identifierManager中获取到, 那获取到之后再检查, 是否是import的, 是返回
            // 否则, 认为是最初的包名, 检查是否是最初的包名, 是, 返回true
            // TODO 解决非静态函数的this的对象注入问题
            // 其实 a.b-> get_member a b 其实就可以完成了
            // 其次 只要分辨是否是static 就可以了
            // 这里主要要解决的问题是, 如果第一个前面的路径都是static的(package/file/class)路径的话, 合并成一条
            shortPreStaticIdentifierLink(identifier, context);
            PossibleCallableSupplier callableSupplier = memberManager.createPossibleCallable(
                    identifier.getPosition(), identifier.getValue());
            if (callableSupplier != null) {
                return callableSupplier;
            }
            // 没找到... 是重载()callable declare运算符
            MemberSupplier memberSupplier = forFirstIdentifier(context, identifier);
            return possibleCallableSupplierForOperatorReload(context, memberSupplier, identifier);
        } else {
            PossibleCallableSupplier possibleCallable = memberType.findPossibleCallable(
                    identifier.getPosition(), identifier.getValue());
            if (possibleCallable != null) {
                return possibleCallable;
            }
            MemberSupplier memberSupplier = memberType.find(identifier.getPosition(), identifier.getValue());
            return possibleCallableSupplierForOperatorReload(context, memberSupplier, identifier);
        }
    }

    private static void shortPreStaticIdentifierLink(SourceString identifier, ExpressionContext context) {
        if (true) {
            throw new UnfinishedException();
        }
        // TODO org.harvey.func(); 第一个 需要 转为 org.harvey.func, 而不是 org
        MemberManager memberManager = context.getEnvironment().getMemberManager();
        boolean isLocal = false;// memberManager.isLocal(identifier);
        if (isLocal) {
            // 不能short
            return;
        }
        DIdentifierManager identifierManager = context.getEnvironment().getIdentifierManager();
        ReferenceElement reference = identifierManager.getReference(
                new FullIdentifierString(identifier.getPosition(), identifier.getValue()));
        if (reference == null) {
            // 没有找到
            // 以第一个文件处理
            context.getEnvironment();
        } else {
            boolean declarationInFile = identifierManager.isDeclarationInFile(reference);
            if (declarationInFile) {
                // 是 declare在本文件中的元素, 类名也是declare的元素啊
                // 那就以reference, 获取declare的path
                FullIdentifierString fullIdentifier = identifierManager.getIdentifier(reference);
            } else {
                // 是 import 进来的元素, context往下眺, 跳到非file级别的
                // TODO identifier manager 的 逻辑太过混乱, 需要重构!

            }
        }
    }


    /**
     * @param context read only
     */
    private static MemberSupplier forFirstIdentifier(ExpressionContext context, SourceString identifier) {
        MemberSupplier memberSupplier = context.getEnvironment()
                .getMemberManager()
                .create(identifier.getPosition(), identifier.getValue());
        if (memberSupplier == null) {
            // 没找到...
            throw new AnalysisExpressionException(identifier.getPosition(), "unknown identifier");
        }
        return memberSupplier;
    }

    /**
     * <pre>{@code
     *  context.addIdentifier(memberSupplier);
     *  context.addIdentifier(new NormalOperatorString(identifier.getPosition(),Operator.GET_MEMBER));
     *  // 下面的操作移到外界
     *  // context.addIdentifier(callableSupplier);
     *  // context.addIdentifier(new CompileOperatorString(identifier.getPosition(), CompileOperatorString.CompileOperator.INVOKE));
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
}
