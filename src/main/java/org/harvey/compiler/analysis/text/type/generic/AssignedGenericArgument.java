package org.harvey.compiler.analysis.text.type.generic;

import lombok.Getter;

import java.util.Map;

/**
 * 都可以比. identifier可以放default, 也可以放assign后的值
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-04 19:36
 */
@Getter
public class AssignedGenericArgument {
    // 处于identifier的位置, 实则不是identifier
    private final AssignedGenericType lower;
    private final AssignedGenericType identifier;
    private final AssignedGenericType upper;

    public AssignedGenericArgument(AssignedGenericType identifier, AssignedGenericType lower,
                                   AssignedGenericType upper) {
        this.identifier = identifier;
        this.lower = lower;
        this.upper = upper;
    }

    /**
     * 上下限范围更小
     */
    public static boolean rangeSmaller(AssignedGenericArgument smaller, AssignedGenericArgument larger,
                                       Map<String, NormalType> register) {
        if (smaller == larger) {
            return true;
        }
        return AssignedGenericType.isExtends(larger.lower, smaller.lower, register) &&
                AssignedGenericType.isExtends(smaller.upper, larger.upper, register);
    }

    public boolean isLegalBound(Map<String, NormalType> register) {
        // 带泛型子类到父类泛型的转变->赋值
        // class A<T1,T2> extends Super<T1>{}
        // class Super<T extends B>
        // 1. 已知 T1
        // 2. 要求 T1 extends B
        // 但是 T1 不 extends B
        // 矛盾
        // 所以不行
        // 检查父亲就行, 因为泛型的范围要么不变, 要么层层递减
        // 不用检查祖父类
        // T1和T2可以注册到Register一样的关系表里去

        // 2.
        // 已知
        // A<T1 extends C<T2>,T2> extends Super<T1>
        // Super<T extends B>
        // C<T> extends B
        // 1. T1 extends C<T2> 注册
        // 2. T2 extends null 注册
        // 3. Super<T1>检查正确与否
        //      1. 条件引入 Super<T1>
        //      2. 条件引入 Super<T extends B>
        //      3. T1!=B, T1查表, T1 extends C<T2>
        //      4. C<T2>!=B, C<T2> 查表, C<T2> extends B, C带有泛型, B不带泛型, 所以是可以的
        //      5. B==C, 故合法
        // 所以 A<B,C> extends Super<B>
        // 转变成功的, 这一步是的
        // 普通类型到父类, 并查集类似算法
        boolean lowerCorrect = lower == null || AssignedGenericType.isExtends(lower, identifier, register);
        boolean upperCorrect = upper == null || AssignedGenericType.isExtends(identifier, upper, register);
        return lowerCorrect && upperCorrect
                ;
    }


}

