package org.harvey.compiler.execute.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;

/**
 * 尝试从众多return中知道返回值的类型...?
 * 那就依据另一边来做基础判断, 然后比较这里面的....
 * 例如函数参数, 或者赋值目标的类型, 或者强制类型转换
 * 参考Java:
 * <pre>{@code
 * private static int a = ((Supplier<Integer>) () -> {
 *         int a = 12;
 *         a++;
 *         int b = -a;
 *         return b + 2;
 *     }).get();
 * }</pre>
 * 或则
 * <pre>{@code
 * private static int a = ((Supplier<Integer>) ExpressionFactory::sup).get();
 *
 * public static int sup() {
 *     int a = 12;
 *     a++;
 *     int b = -a;
 *     return b + 2;
 * }
 * }</pre>
 * 当然可以不用, 而是依据里面的返回值真的一一推敲, 这样很困难...qwq
 * <pre>{@code
 *   Function<Integer, Long> func = Long::valueOf;
 *   if (x > 0) {
 *       int num = 1;
 *       // 干脆只有final的可以传入lambda表达式, 然后以参数->字段的形态, 值传递的方式传入Lambda表达式
 *       func = n -> Long.valueOf(n + num);
 *   }
 *   func.apply(12);
 * }</pre>
 */
@Getter
@AllArgsConstructor
public class LambdaExpression extends ComplexExpression {
    private final SourceString[] arguments;
    /**
     * 1. null 表示是一行的, 其函数体也是表达式, 也能被解析
     * 2. 否则, 去除了{}的内部表达式
     */
    private  SourceTextContext body;


}
