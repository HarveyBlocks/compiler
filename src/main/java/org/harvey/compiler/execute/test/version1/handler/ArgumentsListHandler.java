package org.harvey.compiler.execute.test.version1.handler;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.element.ArgumentWarp;
import org.harvey.compiler.execute.test.version1.element.CallableInvokeResultSupplier;
import org.harvey.compiler.execute.test.version1.element.CompileOperatorString;
import org.harvey.compiler.execute.test.version1.env.CallableArgumentOuter;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.execute.test.version1.msg.UnsureConstructorSupplier;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.execute.test.version1.pipeline.TodoTask;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * 所有的Callable Argument的init{@link ArgumentWarp}的环境
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 16:45
 */
public class ArgumentsListHandler implements ExpressionHandler {
    private static void resultTypeCheck(ExpressionContext context, PossibleCallableSupplier possibleCallable) {
        MemberType determinedType = null;
        if (context.typeDetermined()) {
            // 由于多类型一定是var, 所以一定是单个类型
            determinedType = context.getEnvironment().determinedType();
        }
        if (possibleCallable instanceof UnsureConstructorSupplier) {
            if (determinedType == null) {
                throw new AnalysisExpressionException(possibleCallable.getPosition(), "can not make sure constructor");
            }
            ((UnsureConstructorSupplier) possibleCallable).makeSure(determinedType);
        } else {
            possibleCallable.eliminateImpossibleResultType(determinedType);
        }

    }

    private static ArgumentWarp[] injectArguments(
            CallableArgumentOuter callableArgumentOuter,
            List<SourceString> argumentsSource,
            ExpressionContext context,
            PossibleCallableSupplier possibleCallable) {
        if (argumentsSource.isEmpty()) {
            return new ArgumentWarp[0];
        }

        ListIterator<SourceString> iterator = argumentsSource.listIterator();
        int argIndex = 0;
        List<ArgumentWarp> warps = new ArrayList<>();
        while (iterator.hasNext()) {
            ArrayList<SourceString> argument = SourceTextContext.skipUntilComma(iterator, ArrayList::new);
            if (iterator.hasNext()) {
                if (!CollectionUtil.skipIf(iterator, s -> Operator.COMMA.nameEquals(s.getValue()))) {
                    throw new AnalysisExpressionException(iterator.next().getPosition(), "expected , for argument");
                }
                if (!iterator.hasNext()) {
                    throw new AnalysisExpressionException(iterator.previous().getPosition(), "not except empty");
                }
            }
            if (argument.isEmpty()) {
                throw new AnalysisExpressionException(iterator.previous().getPosition(), "except more argument");
            }
            ArgumentWarp argumentWrap = new ArgumentWarp(argument.get(0).getPosition(), argIndex,
                    callableArgumentOuter
            );
            warps.add(argumentWrap);
            context.addTodoTask(new TodoTask(callableArgumentOuter, argument, argumentWrap));
            argIndex++;
        }
        // 结束了
        ArgumentWarp[] argumentWarps = warps.toArray(new ArgumentWarp[0]);
        // 重新设置possible
        // 确定了possible param length
        possibleCallable.eliminateImpossibleByLength(argumentWarps.length);
        return argumentWarps;
    }

    @Override
    public boolean handle(ExpressionContext context) {
        if (!context.hasPrevious()) {
            return false;
        }
        ExpressionElement expectCallableSupplier = context.getPrevious();
        PossibleCallableSupplier possibleCallable;
        if (!(expectCallableSupplier instanceof PossibleCallableSupplier)) {
            return false;
        } else {
            possibleCallable = (PossibleCallableSupplier) expectCallableSupplier;
        }
        context.mark();
        SourceTextContext genericListSource = SourceTextContext.skipNest(
                context, Operator.GENERIC_LIST_PRE.getName(), Operator.GENERIC_LIST_POST.getName(), false);
        // 是函数调用, 就获取一整个括号中的元素
        // 如果没有括号?
        SourceTextContext argumentsSource = SourceTextContext.skipNest(
                context, Operator.CALL_PRE.getName(), Operator.CALL_POST.getName(), false);
        // 去掉括号
        if (argumentsSource.isEmpty()) {
            context.returnToAndRemoveMark();
            return false;
        }
        context.removeMark();
        // 确定是函数调用了
        resultTypeCheck(context, possibleCallable);
        // 解析genericList
        context.mark();
        MemberType[] genericList;
        try {
            genericList = injectGenerics(genericListSource, context, possibleCallable);
        } catch (Throwable t) {
            context.returnToMark();
            return false;
        } finally {
            context.removeMark();
        }

        // 确实是函数调用了
        context.add(new CompileOperatorString(
                expectCallableSupplier.getPosition(),
                CompileOperatorString.CompileOperator.INVOKE
        ));

        // 解析arguments
        SourcePosition argumentStart = argumentsSource.removeFirst().getPosition();
        SourcePosition argumentEnd = argumentsSource.removeLast().getPosition();
        CallableArgumentOuter callableArgumentOuter = new CallableArgumentOuter(
                possibleCallable,
                genericList,
                context.getEnvironment()
        );
        ArgumentWarp[] argumentWarps = injectArguments(
                callableArgumentOuter, argumentsSource, context, possibleCallable);
        /*argument 结束的类型, 方便下次进行管理*/
        CallableInvokeResultSupplier invoke = new CallableInvokeResultSupplier(
                argumentEnd, genericList, argumentWarps, possibleCallable);
        context.add(invoke);
        return true;
    }

    private MemberType[] injectGenerics(
            SourceTextContext genericListSource, ExpressionContext context, PossibleCallableSupplier possibleCallable) {
        if (genericListSource.isEmpty()) {
            return new MemberType[0];
        }
        ParameterizedType<ReferenceElement>[] genericList = GenericFactory.genericList(possibleCallable.getPosition(),
                genericListSource.listIterator(), context.getEnvironment().getIdentifierManager()
        );
        MemberType[] expressionParameterizedTypes = new MemberType[genericList.length];
        for (int i = 0; i < expressionParameterizedTypes.length; i++) {
            ParameterizedType<ReferenceElement> element = genericList[i];
            expressionParameterizedTypes[i] = context.getEnvironment().relateParameterizedType(element);
        }
        possibleCallable.eliminateImpossibleByGenericList(expressionParameterizedTypes);
        return expressionParameterizedTypes;
    }

}
