package org.harvey.compiler.analysis.text.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.LinkedList;
import java.util.List;

/**
 * GenericType{
 * Type Origion
 * <Type,int>[] types_index_map; // index指向Type里注册的可能的类型, -1表示每有使用泛型列表, 1表示哪种泛型列表
 * }
 * Type{
 * String identifier; // 原了类型
 * Type[] genericTypeArgs; // 定义在源码里的参数列表类型
 * Type[][] 注册在这里的几种可能的泛型类型
 * }
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-02 20:43
 */
@Deprecated
public class GenericTypeTest1 {


    private static SourcePosition sp = new SourcePosition(0, 0);

    private static void phaseGenericType(List<TypeDefinitionMessage> register,
                                         LinkedList<SourceTextContext> tobePhase) {
        SourceTextContext type = tobePhase.removeFirst();
        SourceString first = type.removeFirst();
        if (!(first.getType() == SourceStringType.IDENTIFIER)) {
            throw new AnalysisExpressionException(first.getPosition(), "First must be a identifier for a type");
        }
        TypeDefinitionMessage typeDefinition = new TypeDefinitionMessage(first.getValue());
        register.add(typeDefinition);
        // 如果是GenericType, 最后一定是一组<>
        // 可以用深度优先来做啦
        tobePhase.add(null);
        for (SourceString sourceString : type) {
            SourcePosition position = sourceString.getPosition();
            String value = sourceString.getValue();
            switch (sourceString.getType()) {
                case IDENTIFIER:
                    break;
                case OPERATOR:
                    if (Operator.COMMA.nameEquals(value)) {
                        // 逗号了

                    } else if (Operator.GENERIC_LIST_PRE.nameEquals(value)) {
                        // 开始符

                    } else if (Operator.GENERIC_LIST_POST.nameEquals(value)) {
                        // 结束符

                    }
                    break;
                default:
                    throw new AnalysisExpressionException(position, "Unknown in generic: " + value);
            }
        }
    }

    private static SourceTextContext initContext() {
        SourceTextContext context = new SourceTextContext();
        addToContext(context, SourceStringType.IDENTIFIER, "A");
        addToContext(context, SourceStringType.OPERATOR, "<");
        addToContext(context, SourceStringType.IDENTIFIER, "B");
        addToContext(context, SourceStringType.OPERATOR, ",");
        addToContext(context, SourceStringType.IDENTIFIER, "C");
        addToContext(context, SourceStringType.OPERATOR, "<");
        addToContext(context, SourceStringType.IDENTIFIER, "D");
        addToContext(context, SourceStringType.OPERATOR, ",");
        addToContext(context, SourceStringType.IDENTIFIER, "E");
        addToContext(context, SourceStringType.OPERATOR, ">");
        addToContext(context, SourceStringType.OPERATOR, ",");
        addToContext(context, SourceStringType.IDENTIFIER, "F");
        addToContext(context, SourceStringType.OPERATOR, "<");
        addToContext(context, SourceStringType.IDENTIFIER, "G");
        addToContext(context, SourceStringType.OPERATOR, ">");
        addToContext(context, SourceStringType.OPERATOR, ">");
        return context;
    }

    private static void addToContext(SourceTextContext context, SourceStringType keyword, String internal) {
        context.add(new SourceString(keyword, internal, sp = SourcePosition.moveToEnd(sp, internal + " ")));
    }

    public void demo() {
        SourceTextContext context = initContext();
        List<TypeDefinitionMessage> register = new LinkedList<>();
        LinkedList<SourceTextContext> tobePhase = new LinkedList<>();
        SourceTextContext type = new SourceTextContext(context);
        tobePhase.add(type);
        while (!tobePhase.isEmpty()) {
            phaseGenericType(register, tobePhase);
        }
    }

    public interface Comparable<A extends Comparable<A>> {
        int compare(A a, A b);
    }

    @AllArgsConstructor
    @Getter
    public static class GenericElementDefinition {
        private final String genericName;
        private final GenericElementDefinition defaultValue = null; // 暂且不考虑?
        private final GenericElementDefinition upperBound = null;// 暂且不考虑
        private final GenericElementDefinition lowerBound = null;
    }

    public static class X<A extends X<B, A>, B extends X<A, B>> {
        // 1. 先获取到名字 A,B
        // 2. 解析发现, X的B的上界是X<,>
        // 3. 上界X的参数需要A,B
        // 4. 上界X的第一个参数A, 没有上下界
        // 5. 上界X的第二个参数B, 其上界是X
        // 6. X需要什么? X的第一个参数是...
        // 为什么不会报错呢? 因为Java的类型擦除导致其只能检查两遍吗?
        // 不使用类型擦除的话这就是循环定义了喽
        // 那就强制要求, 泛型类型的参数定义里不能将上下界定为自己...吗?
        public void x() {

        }


        class M<A extends M<A, B>, B extends M<B, A>> {

        }

        class Son1 extends X<Son2, Son1> {

        }

        // 怎么保存... 直接存STC算了...
        // LoadTo
        // 例如
        // X<T1,T2,T3 extends T1,A super T4,T5 extends X<T3,T4,T2,T5,T1,T6>,T6>
        // 本地: T1,T2,T3,T4,T5,T6
        // upper
        // null,null,T1, null, X<T3,T4,T2,T5,T1,T6>,null
        // lower
        // null,null,null, A, null,null
        // 需要检查X<T3,T4,T2,T5,T1,T6>, 带入原定义
        // X<T1,T2,T3 extends T1,A super T4,T5 extends X<T3,T4,T2,T5,T1,T6>,T6>, 得
        // X<T3,T4,T2 extends T3,A super T5,T1 extends X<T2,T5,T4,T1,T3,T6>,T6>
        // 这里的T2需要extends T3, 但并没有
        // 这里的T5需要super A, 但并没有
        // 这里的T1需要extends<T2,T5,T4,T1,T3,T6>, 但是T1没有
        // 所以
        // X<T1 extends X<T2,T5,T4,T1,T3,T6>,T2 extends T3,T3 extends T1,A super T4, A super T5 extends X<T3,T4,T2,T5,T1,T6>,T6>
        // T2 extends T3 extends T1 extends X<T2,T5,T4,T1,T3,T6>
        // A super T4
        // A super T5 extendsX<T3,T4,T2,T5,T1,T6>
        // 现检查  X<T2,T5,T4,T1,T3,T6>
        // 1. 要求T4 extends T3
        // 2. 要求T5 extends T3, 除非T3 extends X<T3,T4,T2,T5,T1,T6.>
        // 2. 要求A super T1
        class Son2 extends X<Son1, Son2> {
            // 检查是否合法
            // 已知: Son1 extends X<Son1,Son2>
            // 1. 另A=Son1, B=Son2带入X<A extends X<B, A>, B extends X<A, B>>得
            //      X<Son1 extends X<Son2,Son1>,Son2 extends X<Son1,Son2>>
            // 2. 检查Son1 extends X<Son2, Son1>是否正确, 正确
            // 3. 检查Son2 extends X<Son1,Son2>是否正确,  正确
            // 4. X<Son1,Son2>正确
        }
    }

    public static class AAA implements Comparable<AAA> {
        @Override
        public int compare(AAA aaa, AAA b) {
            return 0;
        }
    }

    public static class Bounds<A extends LinkedList<B>, B> {
    }

    // 声明泛型时的对泛型类型的解析
    // 1. 解析列表第一层(无序)
    // 2. 解析上下界
    // 3. 解析默认值
    public static class TestGenericInJava<A, B, C, E,
            D extends Bounds<F, C>, F extends LinkedList<C>> {
        // TestGenericInJava
        //[A,B,C,E,D,F]
        // N,N,N,N,up=LinkedList,N
        // D:
        // up=LinkedList(已经被注册),
        // index=LinkedList.class.addGeneric(F(什么类型? 要加吗)),F
        // TestGenericInJava实例化成一个类型的时候, LinkedList.class.addGeneric(F(什么类型? 要加吗)) // 如果已经存在, 返回老类型的index
        // 现阶段定义的时候, 发现F其实在同样的参数列表里
        // 可以发现, 在带泛型的参数类型的声明种, 第一层, 永远都是泛型的声明
        // 当涉及默认值, 或者是上下界的时候, 往往就会是以泛型参数使用的形式
        // 那么此时, 需要把在哪个上下界涉及的类型哪里注入可能的情况吗?
        // 不用啊, 需要保存一个规则啊
        // 当在实例化TestGenericInJava的时候, 填入了F后, 就要填入D
        // 要怎么保存这个规则呢...
        // TODO 要认识到上下界的规则会有多复杂?
        // 由于第一层用来定义泛型
        // 也只有第一层定义泛型, 所以, 上下界如果要用到泛型, 一定是用到第一层的泛型
        // 而且泛型的上下界, 泛型的默认值, 都要在第一层决定, 所以, 第一层的名字知道之后, 泛型的信息都可以知道了
        // 1. 获取泛型列表的名词
        // 2. 泛型的上下界
        // 3. 定义上下界的过程中, 需要使用到另一个泛型
        // 4. 如果需要使用到另一个泛型的上下限规则, 就获取哪个泛型的规则, 例如A 的上下界的定义需要有上下界
        // 5. 就先去决定另一个的上下界
        // 6. 由于这是源码层面的定义, 所以不会出现递归的情况
    }

    /**
     * 定义类时构造出这个类型, 相当于一个小的注册中心
     * List<A,B,C,D<E>>
     */
    public static class TypeDefinitionMessage {
        private final String origin;
        // 第一个integer存原有的Type中的类型
        // 后一个Integer存Type类型中的泛型列表的第几种情况
        private final LinkedList<GenericElementDefinition> genericList = new LinkedList<>();

        public TypeDefinitionMessage(String origin) {
            this.origin = origin;
        }

        public void add(String aaa) {
            genericList.add(new GenericElementDefinition(aaa));
        }
    }

    public static class TypeDefinition {
        private final String origin;
        // 第一个integer存原有的Type中的类型
        // 后一个Integer存Type类型中的泛型列表的第几种情况
        private final GenericElementDefinition[] genericList;
        // 存放各种可能的情况
        private final LinkedList<GenericElementDefinition[]> register = new LinkedList<>();

        public TypeDefinition(TypeDefinitionMessage origin) {
            this.origin = origin.origin;
            this.genericList = origin.genericList.toArray(new GenericElementDefinition[]{});
        }

        public void add(GenericElementDefinition[] aaa) {
            register.add(aaa);
        }

        public boolean corresponding(GenericElementDefinition[] aaa) {
            // TODO, 是否和GenericElementDefinition[]完成正确的对应
            return false;
        }
    }

    // 使用时
    public static class Pair {
        public static TypeDefinitionMessage outer;
    }

}
