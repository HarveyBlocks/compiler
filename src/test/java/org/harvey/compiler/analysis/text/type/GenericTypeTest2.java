package org.harvey.compiler.analysis.text.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.calculate.Operators;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.analysis.text.type.generic.AssignedGenericArgument;
import org.harvey.compiler.analysis.text.type.generic.AssignedGenericType;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-02 20:43
 */
public class GenericTypeTest2 {
    private static SourcePosition sp = new SourcePosition(0, 0);

    private static Map<String, NormalType> register() {
        // 1. 第一层是声明
        // 2. 第二层
        NormalType[] type = new NormalType[17];
        type[0] = new NormalType("A");
        type[1] = new NormalType("C");
        type[2] = new NormalType("C1", type[1]);
        type[3] = new NormalType("C2", type[2]);
        type[4] = new NormalType("B2", type[3]);
        type[5] = new NormalType("B1", type[4]);
        type[6] = new NormalType("B", type[5]);
        type[7] = new NormalType("E");
        type[8] = new NormalType("E1", type[7]);
        type[9] = new NormalType("E2", type[8]);
        type[10] = new NormalType("F2", type[9]);
        type[11] = new NormalType("F1", type[10]);
        type[12] = new NormalType("F", type[11]);
        type[13] = new NormalType("D2", type[12]);
        type[14] = new NormalType("D1", type[13]);
        type[15] = new NormalType("D", type[14]);
        type[16] = new NormalType("G");
        return Arrays.stream(type).collect(Collectors.toMap(NormalType::getName, e -> e));
    }

    private static void print(SourceTextContext context) {
        for (SourceString sourceString : context) {
            System.out.print(sourceString.getValue() + " ");
        }
        System.out.println();
    }

    private static SourceTextContext initContext() {
        // A < T1 , T2 extends A < T2 , T3 , T1 > , A < T2 , T3 , T1 > super T3 , B super T4 extends C , T5 = T2 ,
        // D < T2 , T3 > super T6 extends E < T1 > = F < T1 > >
        SourceTextContext context = new SourceTextContext();
        // 关于非第一层能否定义extends
        String src = "A < T1 , T2 extends A < T2 , T3 , T1 > , A < T2 , T3 , T1 > " +
                "super T3 , B super T4 extends C , T5 = T2 , " +
                "D < T2 , T3 > super T6 extends E < T1 > = F < T1 , G < E , F > > > ";// 末尾加空格
        StringBuilder sb = new StringBuilder();
        for (char c : src.toCharArray()) {
            if (c != ' ') {
                sb.append(c);
                continue;
            }
            String value = sb.toString();
            sb = new StringBuilder();
            SourceStringType type;
            if (Keyword.get(value) != null) {
                type = SourceStringType.KEYWORD;
            } else if (Operators.NAME_SET.contains(value)) {
                type = SourceStringType.OPERATOR;
            } else {
                type = SourceStringType.IDENTIFIER;
            }
            addToContext(context, type, value);

        }
        return context;
    }

    private static void addToContext(SourceTextContext context, SourceStringType type, String value) {
        context.add(new SourceString(type, value, sp = SourcePosition.moveToEnd(sp, value + " ")));
    }

    private static org.harvey.compiler.analysis.text.type.generic.NormalType registerNormal(
            Map<String, org.harvey.compiler.analysis.text.type.generic.NormalType> register, String name,
            org.harvey.compiler.analysis.text.type.generic.NormalType parent) {
        org.harvey.compiler.analysis.text.type.generic.NormalType normalType = new org.harvey.compiler.analysis.text.type.generic.NormalType(
                name(name), parent);
        register.put(name, normalType);
        return normalType;
    }

    private static org.harvey.compiler.analysis.text.type.generic.NormalType registerGeneric(
            Map<String, org.harvey.compiler.analysis.text.type.generic.NormalType> register,
            String name, org.harvey.compiler.analysis.text.type.generic.NormalType parent,
            org.harvey.compiler.analysis.text.type.generic.GenericArgument[] args) {
        return register.put(name,
                new org.harvey.compiler.analysis.text.type.generic.GenericType(name(name), parent, args));
    }

    private static SourceString name(String name) {
        return new SourceString(
                SourceStringType.IDENTIFIER, name, sp = SourcePosition.moveToEnd(sp, name + " ")
        );
    }

    @Test
    public void testGenericType() {
        SourceTextContext context = initContext();
        print(context);
        Map<String, NormalType> register = register();
        // B<B1<B2<C2<C1<C
        // D<D1<D2<F<F1<F2<E2<E1<E
        GenericType genericType = GenericTypeFactory.create(context, register);
        System.out.println(genericType);
    }

    @Test
    public void demo() {
        // 想测试一下
        // A<T1 extends A<T1,T2>,T2 extends A<T2,T1>>
        // Son1 extends A<Son1,Son2>
        // Son2 extends A<Son2,Son1>
        Map<String, org.harvey.compiler.analysis.text.type.generic.NormalType> register = new HashMap<>();


        // TODO 只检查了extends的, 没有implements的
        org.harvey.compiler.analysis.text.type.generic.GenericArgument[] genericArgs = new org.harvey.compiler.analysis.text.type.generic.GenericArgument[2];
        org.harvey.compiler.analysis.text.type.generic.GenericType typeA = new org.harvey.compiler.analysis.text.type.generic.GenericType(
                name("A"), null, genericArgs);
        register.put("A", typeA);
        genericArgs[0] = new org.harvey.compiler.analysis.text.type.generic.GenericArgument(name("T1"), null, typeA,
                null);
        genericArgs[1] = new org.harvey.compiler.analysis.text.type.generic.GenericArgument(name("T2"), null, typeA,
                null);
        AssignedGenericArgument[] son1AArg = new AssignedGenericArgument[2];
        AssignedGenericArgument[] son2AArg = new AssignedGenericArgument[2];
        org.harvey.compiler.analysis.text.type.generic.NormalType son1 = registerNormal(register, "Son1",
                new AssignedGenericType(typeA, son1AArg));
        org.harvey.compiler.analysis.text.type.generic.NormalType son2 = registerNormal(register, "Son2",
                new AssignedGenericType(typeA, son2AArg));
        son1AArg[0] = new AssignedGenericArgument(new AssignedGenericType(son1, null), null, null);
        son1AArg[1] = new AssignedGenericArgument(new AssignedGenericType(son2, null), null, null);
        son2AArg[0] = new AssignedGenericArgument(new AssignedGenericType(son2, null), null, null);
        son2AArg[1] = new AssignedGenericArgument(new AssignedGenericType(son1, null), null, null);
        // TODO 要检查nameEquals, 时刻要检查, 只检查一个地方不够
        System.out.println(new AssignedGenericType(son1, null).registerCorrectly(register));
        System.out.println(
                AssignedGenericType.isExtends(new AssignedGenericType(son1, null), new AssignedGenericType(son2, null),
                        register));
    }

}

/**
 * 由于不考虑基本数据类型, 也就没有什么无符号有符号, 就是说, 一整个的就是一个Identifier了
 * 存储结构
 */
@AllArgsConstructor
@Getter
class SourceType {
    private SourceString identifier;
    private SourceString[] genericIdentifiers; // 泛型名称表
    private SourceTextContext genericDeclares;

}

@Getter
@AllArgsConstructor
class NormalType {
    private final String name;
    private NormalType parent;

    NormalType(String name) {
        this(name, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NormalType)) {
            return false;
        }
        NormalType that = (NormalType) o;
        boolean sameName = Objects.equals(getName(), that.getName());
        if (sameName) {
            boolean sameParent = Objects.equals(getParent(), that.getParent());
            if (!sameParent) {
                throw new IllegalStateException("同名但不同的父类, 合理吗, 不合理???");
            }
        }
        return sameName;
    }

    @Override
    public String toString() {
        return "NormalType{" +
                "name='" + name + '\'' +
                ", parent='" + parent.getName() +
                "'}";
    }
}

@Getter
class GenericType extends NormalType {

    private final GenericArgument[] args;

    GenericType(String name, GenericArgument[] args) {
        super(name);
        this.args = args;
    }

    public GenericType(NormalType origin, GenericArgument[] args) {
        super(origin.getName(), origin.getParent());
        this.args = args;
    }

    /**
     * 要构建(结构合法)
     * 然后注册
     * 注册之后才能判断是否合法(逻辑合法)
     * <p>
     * 本方法用于声明泛型的时候检查上下界是否合法
     */
    public void valid(Map<String, GenericType> pool) {
        // 检查泛型参数是否有效

        if (args == null || args.length == 0) {
            return;
        }

        Set<String> nameSet = new HashSet<>();
        // 构造nameSet
        // 1. name不能重复
        for (GenericArgument arg : args) {
            String argName = arg.getName().getName();
            if (nameSet.contains(argName)) {
                throw new IllegalStateException("名字不能重复");
            }
            nameSet.add(argName);
        }
        // 2. 参数的上下界和默认值是否合法
        for (GenericArgument arg : args) {
            // TODO
        }
    }

    /**
     * 对于某个泛型的使用是否复合本类描述的泛型
     *
     * @param tobeTested  等待被判断的结构
     * @param description 对每个泛型参数类型的描述, 例如它是否继承自XX类
     *                    必须包含已经注册在import和本文件的所有类,
     *                    如果Key是泛型类型, 那么需要描述出泛型的定义信息
     */
    public boolean isLegal(GenericType tobeTested, Map<String, NormalType> description) {
        if (this == tobeTested) {
            return true;
        }
        // 这个是强制性的, 在方法外必须确定
        assert getName().equals(tobeTested.getName());
        // 如果是有默认参数的情况...就不需要一样多
        if (args.length < tobeTested.args.length) {
            return false;
        }

//        Map<GenericType, GenericType> nameMap = GenericTypeFactory.createMap(args, tobeTested.args);
        // 使用的时候一定不会有extends了....吗?
        // 定义: X<A extends X<A,B>,B extends X<A,B>>
        // 判断: X<Son1<T>,Son2<T>>是否符合
        // 已知 Son1<T> extends X<Son1<T>,Son2<T>>
        //      Son2<T> extends X<Son2<T>,Son1<T>>
        for (int i = 0; i < args.length; i++) {
            GenericArgument thisArg = args[i];
//            DeclareGenericArgument assignedArg = GenericTypeFactory.assign(thisArg, nameMap);
            // TODO
//            if (!GenericArgumentFactory.legal(assignedArg, description)) {
//                return false;
//            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "GenericType='" + getName() + '\'' +
                "\n" +
                (args == null ? "null" : Arrays.stream(args).map(arg -> arg + "\n").collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GenericType)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GenericType that = (GenericType) o;
        return Arrays.equals(getArgs(), that.getArgs());
    }


}

@Getter
@AllArgsConstructor
class GenericArgument {
    private final GenericType name;
}

/**
 * 逻辑结构
 */
@Getter
class DeclareGenericArgument extends GenericArgument {

    // 非常遗憾, 使用递归
    private final GenericType upper; // 上界
    private final GenericType lower; // 下界
    private final GenericType defaultGeneric;

    public DeclareGenericArgument(GenericType name, GenericType upper, GenericType lower, GenericType defaultGeneric) {
        super(name);
        this.upper = upper;
        this.lower = lower;
        this.defaultGeneric = defaultGeneric;
    }

    @Override
    public String toString() {
        return "DeclareGenericArgument'" + getName() + '\'' +
                (upper != null ? (", upper=" + upper) : "") +
                (defaultGeneric != null ? (", default=" + defaultGeneric) : "") +
                (lower != null ? (", lower=" + lower) : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeclareGenericArgument)) {
            return false;
        }
        DeclareGenericArgument that = (DeclareGenericArgument) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getUpper(), that.getUpper()) &&
                Objects.equals(getLower(), that.getLower()) &&
                Objects.equals(getDefaultGeneric(), that.getDefaultGeneric());
    }
}

class SimpleGenericArgument extends GenericArgument {
    public SimpleGenericArgument(GenericType name) {
        super(name);
    }
}

class GenericTypeFactory {
    private GenericTypeFactory() {
    }

    public static GenericType create(SourceTextContext context, Map<String, NormalType> register) {
        // 由于是下届, 上下届的概念来源于继承, 只有类才能继承
        // 所以基本数据类型不能作为上下界
        SourceString first = context.removeFirst();
        assert first.getType() == SourceStringType.IDENTIFIER;
        // 区别A< > 和 A
        NormalType origin = register.get(first.getValue());
        DeclareGenericArgument[] args = GenericArgumentFactory.create(context, register);
        return origin == null ?
                new GenericType(first.getValue(), args) :
                new GenericType(origin, args);
    }


    /**
     * 非常遗憾, 递归
     *
     * @param template 模板
     * @param truthMap 赋值表
     * @return 赋值之后的结果
     */
    public static DeclareGenericArgument assign(DeclareGenericArgument template,
                                                Map<GenericType, GenericType> truthMap) {
        if (template == null) {
            return null;
        }
        /*String name = ;
        if (name == null) {
            // 没有对应的Map信息
            // 说明启用默认值
            // 要怎么完成默认值的替换呢?
            // 答案是在description里补充default的信息
            name = template.getName().getName();
            // Son1 extends X<Son1,Son2>是最终的结果

        }*/
        return new DeclareGenericArgument(
                truthMap.getOrDefault(template.getName(), template.getName()),
                GenericTypeFactory.assign(template.getUpper(), truthMap),
                GenericTypeFactory.assign(template.getLower(), truthMap),
                GenericTypeFactory.assign(template.getDefaultGeneric(), truthMap)
        );
    }

    private static GenericType assign(GenericType template, Map<GenericType, GenericType> truthMap) {
        /*new GenericType(
                truthMap.getOrDefault(template, template).getName(),
                template.getArgs() == null ? null : Arrays.stream(template.getArgs())
                        .map(arg -> GenericTypeFactory.assign(arg, truthMap))
                        .toArray(DeclareGenericArgument[]::new)
        );*/
        return null;
    }

    public static Map<GenericType, GenericType> createMap(DeclareGenericArgument[] template,
                                                          DeclareGenericArgument[] truth) {
        Map<GenericType, GenericType> map = new HashMap<>();
        for (int i = 0; i < template.length && i < truth.length; i++) {
            // 有默认参数不够用咋办...
            map.put(template[i].getName(), truth[i].getName());
        }
        return map;
    }
}

class GenericArgumentFactory {
    private GenericArgumentFactory() {
    }

    public static DeclareGenericArgument createOne(SourceTextContext eachDeclare, Map<String, NormalType> register) {
        // [] super [] extends [] = []
        int superIndex = -1;
        int extendsIndex = -1;
        int assignIndex = -1;
        int i = 0;
        for (SourceString ss : eachDeclare) {
            switch (ss.getValue()) {
                case "super":
                    assert superIndex == -1;
                    superIndex = i;
                    break;
                case "extends":
                    assert extendsIndex == -1;
                    extendsIndex = i;
                    break;
                case "=":
                    assert assignIndex == -1;
                    assignIndex = i;
                    break;
            }
            i++;
        }
        // 有super, 就有下限
        GenericType lower = superIndex != -1 ?
                validBound(eachDeclare.subContext(0, superIndex), register) : null;
        // 有=就有默认值
        GenericType defaultGeneric = assignIndex != -1 ?
                validBound(eachDeclare.subContext(assignIndex + 1), register) : null;
        GenericType upper = null;
        if (extendsIndex != -1) {
            // 有Upper
            // Lower super T extends upper = Default;
            int end = assignIndex != -1 ? assignIndex : eachDeclare.size();
            upper = validBound(eachDeclare.subContext(extendsIndex + 1, end), register);
        }
        int start = superIndex != -1 ? superIndex + 1 : 0;
        int end = extendsIndex != -1 ? extendsIndex : (assignIndex != -1 ? assignIndex : eachDeclare.size());

        if (!hasExtendsRelationship(lower, upper)) {
            throw new IllegalStateException("上下限不具有继承关系");
        }
        if (!hasExtendsRelationship(lower, defaultGeneric) || !hasExtendsRelationship(defaultGeneric, upper)) {
            throw new IllegalStateException("默认值不在上下限之内");
        }
        GenericType name = validName(eachDeclare.subContext(start, end), register);
        return new DeclareGenericArgument(name, upper, lower, defaultGeneric);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean hasExtendsRelationship(GenericType lower, GenericType upper) {

        if (lower == null || upper == null) {
            return true;
        }
        NormalType type = lower;
        while (true) {
            if (type == null) {
                return false;
            } else if (type.equals(upper)) {
                return true;
            }
            type = type.getParent();
        }
    }


    private static GenericType validName(SourceTextContext context, Map<String, NormalType> register) {
        assert !context.isEmpty();
        if (context.size() == 1) {
            SourceString name = context.get(0);
            assert name.getType() == SourceStringType.IDENTIFIER;
            return new GenericType(name.getValue(), null);
        } else {
            return GenericTypeFactory.create(context, register);
        }
    }

    private static GenericType validBound(SourceTextContext context, Map<String, NormalType> register) {
        // 由于是下届, 上下届的概念来源于继承, 只有类才能继承
        // 所以基本数据类型不能作为上下界
        // X<T extends A<M<M>,M<N>>>
        GenericType result = GenericTypeFactory.create(context, register);

        // 上下界的定义不能再复杂了, 只能有名, 不能有上下界和默认值
        if (result.getArgs() == null) {
            return result;
        }
        GenericArgument[] args = result.getArgs();
        GenericArgument[] simpleArg = new SimpleGenericArgument[args.length];
        int i = 0;
        for (GenericArgument arg : args) {
            if (!(arg instanceof DeclareGenericArgument)) {
                simpleArg[i] = arg;
                i++;
                continue;
            }
            DeclareGenericArgument dga = (DeclareGenericArgument) arg;
            assert dga.getLower() == null; // L
            assert dga.getUpper() == null; // L
            assert dga.getDefaultGeneric() == null; // L
            GenericType name = arg.getName();
            simpleArg[i] = new SimpleGenericArgument(name);
            i++;
        }
        return new GenericType(result.getName(), simpleArg);
    }


    public static DeclareGenericArgument[] create(SourceTextContext genericDeclares, Map<String, NormalType> register) {
        if (genericDeclares == null) {
            return null;
        }
        if (genericDeclares.isEmpty()) {
            return new DeclareGenericArgument[0];
        }
        SourceString first = genericDeclares.removeFirst();
        SourceString last = genericDeclares.removeLast();
        assert "<".equals(first.getValue());
        assert ">".equals(last.getValue());
        List<DeclareGenericArgument> result = new LinkedList<>();
        int depth = 0;
        while (!genericDeclares.isEmpty()) {
            SourceTextContext eachDeclare = new SourceTextContext();
            while (!genericDeclares.isEmpty()) {
                SourceString each = genericDeclares.removeFirst();
                String value = each.getValue();
                if ("<".equals(value)) {
                    depth++;
                } else if (">".equals(value)) {
                    depth--;
                }
                assert depth >= 0;
                if (depth == 0) {
                    if (",".equals(value)) {
                        break;
                    }
                }
                eachDeclare.add(each);
            }
            result.add(GenericArgumentFactory.createOne(eachDeclare, register));
        }
        return result.toArray(new DeclareGenericArgument[]{});
    }

    public static boolean legal(DeclareGenericArgument assignedArg, Map<String, NormalType> description) {
        // law 是  L < T < S
        // suspect 是 L<?<S or T
        // L<?<S 只要  L<= L? <= S? <= S 即可返回true
        // 对于T,
        // 只要满足L<T<S, 即可返回true
        // 如果L和S带有泛型, 则为止奈何?
        // 我们需要一种带反省的类型检查方法
        // law的上下限, 其实可以被填充已知类填充, 是的...
        // TODO
        return true;
    }
}