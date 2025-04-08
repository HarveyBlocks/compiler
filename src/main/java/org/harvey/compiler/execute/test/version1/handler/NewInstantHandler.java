package org.harvey.compiler.execute.test.version1.handler;

import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.execute.test.version1.msg.UnsureConstructorSupplier;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.using.ParameterizedType;

/**
 * 用new运算符的处理, 直接转换成一个构造器的{@link PossibleCallableSupplier}的item, 而不是operator
 * <pre>{@code
 * 获取一个类型
 * 或者什么都没有, 能吗? Type a = new<>();
 * 或者什么都没有, 能吗? Type a = (new Type<>)<>();
 * 或者什么都没有, 能吗? Type a = new Type<>();
 * 或者什么都没有, 能吗? Type a = new<>();
 * 或者什么都没有, 能吗? Type a = new<>();
 * }</pre>
 * <pre>{@code
 * new 后面的类型一定是静态的,
 * this.new ThisInner();
 * obj.new ObjInner();
 * }</pre>
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-06 16:08
 */
public class NewInstantHandler implements ExpressionHandler {

    @Override
    public boolean handle(ExpressionContext context) {
        // 将new 转成 类型.constructor的类型 , 返回一个structure
        // (new FullIdentifier.func())[generic list] a()
        // 读一个new
        // 读一个类型
        // 一个类型返回
        //
        SourceString expectNew = context.next();
        if (!Keyword.NEW.equals(expectNew.getValue())) {
            context.previousMove();
            return false;
        }
        // 获取一个类型
        // 或者什么都没有, 能吗? Type a = new<>();
        // 或者什么都没有, 能吗? Type a = (new Type<>)<>();
        // 或者什么都没有, 能吗? Type a = new Type<>();
        // 或者什么都没有, 能吗? Type a = new<>();
        // 或者什么都没有, 能吗? Type a = new<>();
        //
        SourcePosition newPosition = expectNew.getPosition();
        MemberType memberType = null;
        context.mark();
        try {
            ParameterizedType<ReferenceElement> parameterizedType = GenericFactory.parameterizedType(context,
                    context.getEnvironment().getIdentifierManager()
            );
            memberType = context.getEnvironment().relateParameterizedType(parameterizedType);
        } catch (Throwable t) {
            context.returnToMark();
        } finally {
            context.removeMark();
        }
        // new 后面的类型一定是静态的,
        // this.new ThisInner();
        // obj.new ObjInner();
        //
        PossibleCallableSupplier supplier = getSupplier(newPosition, memberType, context);
        context.add(supplier);
        return true;
    }

    private static PossibleCallableSupplier getSupplier(
            SourcePosition newPosition, MemberType instanceTarget, ExpressionContext context) {
        MemberSupplier previousLimit = previousLimit(context);
        if (instanceTarget == null) {
            // new UnsureConstructorSupplier(newPosition,/*GET_MEMBER/THIS/SUPER/STATIC*/)
            // 对于constructor, 其from, 其实没有意义吧?
            return new UnsureConstructorSupplier(newPosition, previousLimit);
        }
        if (previousLimit == null) {
            return instanceTarget.findConstructor(newPosition);
        } else {
            return previousLimit.getType().findInnerTypeConstructor(newPosition, instanceTarget);
        }
    }
    private static MemberSupplier previousLimit(ExpressionContext context) {
        if (!context.expressionHasPrevious(-2)) {
            return null;
        }
        ExpressionElement expectMemberSupplier = context.getPrevious(-2);
        if (expectMemberSupplier instanceof MemberSupplier) {
            return (MemberSupplier) expectMemberSupplier;
        }
        return null;
    }
}
