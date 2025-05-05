package org.harvey.compiler.execute.test.version1.handler;


import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.test.version1.element.LambdaExpressionWrap;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO 比较高级的功能, 放到比较后面的地方再做!
 * <p>
 * 优先级应该要比identifier高, CallPre高, PARENTHESES_PRE高, 比TypeCast高, 因为会无视前面的内容执意尝试->运算符
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:49
 */
public class LambdaExpressionHandler implements ExpressionHandler {

    public static final String PARENTHESES_PRE_NAME = Operator.PARENTHESES_PRE.getName();
    public static final String PARENTHESES_POST_NAME = Operator.PARENTHESES_POST.getName();

    private static IdentifierString[] getArgumentsIfLambda(ExpressionContext context) {
        SourceString next = context.next();
        if (next.getType() == SourceType.IDENTIFIER) {
            if (context.nextIs(expectLambda -> !Operator.LAMBDA.nameEquals(expectLambda.getValue()))) {
                context.previousMove();// identifier
                return null;
            }
            return new IdentifierString[]{new IdentifierString(next)};
        }
        if (!PARENTHESES_PRE_NAME.equals(next.getValue())) {
            context.previousMove();
            return null;
        }
        context.previousMove();
        context.mark();
        SourceTextContext mayIdentifiers = SourceTextContext.skipNest(
                context, PARENTHESES_PRE_NAME, PARENTHESES_POST_NAME, true);
        // 去除前后括号
        if (!context.nextIs(expectLambda -> Operator.LAMBDA.nameEquals(expectLambda.getValue()))) {
            context.returnToAndRemoveMark();
            return null;
        }
        context.removeMark();
        return argumentsList(mayIdentifiers);
    }

    private static IdentifierString[] argumentsList(SourceTextContext mayIdentifiers) {
        mayIdentifiers.removeFirst();
        mayIdentifiers.removeLast();
        if (mayIdentifiers.isEmpty()) {
            return new IdentifierString[0];
        }
        List<IdentifierString> arguments = new ArrayList<>();
        boolean expectIdentifier = true;
        for (SourceString mayIdentifier : mayIdentifiers) {
            SourceType type = mayIdentifier.getType();
            String value = mayIdentifier.getValue();
            if (expectIdentifier && (type == SourceType.IDENTIFIER || type == SourceType.IGNORE_IDENTIFIER)) {
                arguments.add(new IdentifierString(mayIdentifier));
            } else if (expectIdentifier || !Operator.COMMA.nameEquals(value)) {
                throw new AnalysisExpressionException(
                        mayIdentifier.getPosition(),
                        "except " + (expectIdentifier ? "identifier" : "comma")
                );
            }
            expectIdentifier = !expectIdentifier;
        }
        if (expectIdentifier) {
            throw new AnalysisExpressionException(mayIdentifiers.getLast().getPosition(), "except identifier");
        }
        return arguments.toArray(new IdentifierString[0]);
    }

    @Override
    public boolean handle(ExpressionContext context) {
        IdentifierString[] arguments = getArgumentsIfLambda(context);
        if (arguments == null) {
            return false;
        }
        context.previous();
        SourcePosition position = context.next().getPosition();
        if (!context.hasNext()) {
            throw new AnalysisExpressionException(position, "excepted more expression");
        }
        LambdaExpressionWrap warp = new LambdaExpressionWrap(position, arguments);
        context.add(warp);
        // TODO
        // lambda表达式一行? ) or ; or , 就算终止
        // Function<String, Function<String, Function<String, String>>> func = a -> b -> c -> a + b + c;
        // a->a->a->null
        /*String result = ((Function<String, Function<String, Function<String, String>>>)
                a -> b -> c -> a + b + c).apply(
                "a").apply("b").apply("c");*/
        // 确定类型优先级
        // 内部解析完毕, 确定返回值的类型是什么的时候, 还需要上报, 报告outer是否允许这些类型, 或者说, 这些类型在什么情况下允许
        // 那么, 一个类型有多种可能, lambda的参数的类型其实也有多种可能, 因为要依据结果推断啥的, 很难
        // 1. pre 有 cast
        // 2. 在declare上, 前没有更多信息, 后没有更多信息, 说明lambda的类型, 就是declare的类型
        // 3. 在函数的实参上, 要依据函数签名来给出判断
        // 要获取lambda的类型在函数的参数上, 怎么办?
        // 要通过lambda表达式的类型确定lambda表达式是否正确
        // 要通过lambda表达式的类型确定是调用哪个函数
        // 要知道是哪个函数才能知道需要的是哪个lambda表达式的类型
        // 1. 依据函数名和参数个数获取所有的可能的的函数签名
        // 2. 依据其他参数逐渐缩小可能的函数范围, lambda在最后检查
        // 3. lambda表达式所在的类型应当是函数式接口(有且只有一个抽象方法)以此再次排除一些
        // 4. 以目前成功匹配的函数式接口类型去一个个尝试构建lambda表达式
        // 5.1. 构建失败了, 继续下一个
        // 5.2. 构建成功了, 保存那个函数式接口类型, 继续下一个
        // 6.1. 成功的函数式接口类型只有一个, 成功
        // 6.2. 成功的函数式接口类型不止一个, 失败
        // 7. 注意, 如果前面有在泛型参数列表上给定参数的, 把参数考虑进去, 选项可能就唯一了
        // static String a(String s) {return s;}
        // static void a(Consumer<String> consumer) {}
        // static void a(Function<String, Integer> consumer) {}
        // a(x -> a(x));
        OuterEnvironment environment = context.getEnvironment();
        int a = 1 + (int) 1L;
        if (environment.isType(OuterEnvironment.IN_CALLABLE_ARGUMENT)) {

        }
        // context.addLambdaControl();
        return false;
    }
}
