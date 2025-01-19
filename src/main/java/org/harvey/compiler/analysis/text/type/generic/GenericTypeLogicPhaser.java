package org.harvey.compiler.analysis.text.type.generic;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourceString;

import java.util.Map;
import java.util.Set;

/**
 * 检查继承逻辑是否合法
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-04 21:18
 */
public class GenericTypeLogicPhaser {
    private GenericTypeLogicPhaser() {
    }

    // 泛型类型检测工具
    // 泛型中的extends和super的逻辑是否属实
    // 需要检查上下界和默认值的继承关系
    // 定义关系 extends: `<`
    // 应该有 lower <= default <= upper


    /**
     * 赋值
     * 已知泛型声明模板和待检测的模板
     * 获取将泛型全部的检测
     * TemplateClass <T1 extends TemplateClass1<T2,T1>,TemplateClassSon2<T1,T2> super T2, T3 = TemplateClass2<T2>>
     * T1 = TrueClass1, T2=TrueClass2
     * TemplateClass <TrueClass1 extends TemplateClass1<TrueClass2,TrueClass1>,TemplateClassSon2<TrueClass1,TrueClass2> super TrueClass2, TemplateClass2<TrueClass2>>
     * 然后检查上式逻辑是否成立
     * T1 = TrueClass1, T2=TrueClass2, T3 = TrueClass3
     * TemplateClass <TrueClass1 extends TemplateClass1<TrueClass2,TrueClass1>,TemplateClassSon2<TrueClass1,TrueClass2> super TrueClass2, TemplateClass2<TrueClass2>>
     * 然后检查上式逻辑是否成立
     *
     * @return 可以解析是否正确的结构
     */
    public static AssignedGenericType assign(GenericType type, Map<String, NormalType> truthMap) {
        if (type == null) {
            return null;
        }

        Set<String> nameSet = type.buildGenericArgNameSet();
        GenericArgument[] origin = type.getArgs();
        if (origin == null) {
            return null;
        }
        AssignedGenericArgument[] assigned = new AssignedGenericArgument[origin.length];
        // args中的每一个进行赋值
        for (int i = 0; i < origin.length; i++) {
            GenericArgument argument = origin[i];
            SourceString originSource = argument.getName();
            String sourceValue = originSource.getValue();
            if (!nameSet.contains(sourceValue)) {
                // 不是定义的参数
                continue;
            }
            NormalType originType = truthMap.get(sourceValue);
            if (originType != null) {
                // 递归, 非常难受
                AssignedGenericType lower = assign(argument.getLower(), truthMap);
                AssignedGenericType upper = assign(argument.getUpper(), truthMap);
                AssignedGenericType identifier;
                if (originType instanceof GenericType) {
                    identifier = assign((GenericType) originType, truthMap);
                } else if (originType instanceof AssignedGenericType) {
                    identifier = (AssignedGenericType) originType;
                } else {
                    identifier = new AssignedGenericType(originType, null);
                }
                assigned[i] = new AssignedGenericArgument(identifier, lower, upper);
                continue;
            }
            if (argument.getDefaultGeneric() == null) {
                // 没有默认值
                // 如果没有给出的数据
                throw new AnalysisExpressionException(originSource.getPosition(), "需要一个给泛型参数赋值的真值");
            }
            // 没找到, 说明是要使用默认值的
            AssignedGenericType defaultGeneric = assign(argument.getDefaultGeneric(), truthMap);
            AssignedGenericType lower = assign(argument.getLower(), truthMap);
            AssignedGenericType upper = assign(argument.getUpper(), truthMap);
            assigned[i] = new AssignedGenericArgument(defaultGeneric, lower, upper);
        }
        return new AssignedGenericType(type, assigned);
    }

    /**
     * 对于类生命的继承检查
     */
    public static boolean genericExtends(GenericType type, Map<String, NormalType> register) {
        // 1. type.getName;
        // 2. truthFather = type.getParent(); //  父类泛型的使用
        // 3. declareFather = register[truthFather.getName](father也是泛型了) // 父类泛型的声明
        // 4. 证明truthFather使用正确(依据declareFather)
        // 5. 证明truthFather的每个泛型参数(如果有), 都使用正确
        // 问题来了, 父类完全可以是泛型, 看能不能强转, 不能直接就过了
        //    class A<T extends B<C>> {}
        //    class B<T extends C> extends A<T> {}
        //    class C extends B<C> { }
        // 在检查子类的是否符合父类的时候, 父类不能确定已经被构造且检查完毕
        // A完全有可能只是完成了声明结构的划分
        // 所以泛型类型的声明是分阶段的
        //
        // 1. 解析结构并注册(无条件进入)
        // 2. 分析泛型结构的继承结构(全部注册完毕, 此时再出现未知的类, 直接报错类未知; 完成"赋值");
        // 3. 分析泛型结构的继承逻辑
        // 如果类和类之间不是同一个文件就很悲伤了, 又涉及到反复拷贝文件序列化的问题

        // 3. type.arg

        // 1. 注册声明
        // 2. 查看声明使用结构正确
        //       要求被引用的文件已经具备辅助检查结构是否正确的能力
        //       也就是说第一阶段解析的文件要能够描述结构
        // 3. 查看声明使用逻辑正确 (要求被引用的文件已经具备辅助检查逻辑是否正确的能力)
        //       要求被引用的文件已经具备辅助检查结构是否正确的能力
        //       也就是说第一阶段解析的文件要能够描述逻辑, 逻辑关系有:
        //          偏序关系: 继承
        // STC->GenericType结构解析
        // STC->逻辑解析, 见上
        // 问题在于, STC解析成GenericType, 简单吗?
        // 第一遍解析之后, 要怎么返回成STC
        return false;
    }
}
