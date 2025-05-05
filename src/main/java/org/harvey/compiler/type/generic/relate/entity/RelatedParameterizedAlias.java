package org.harvey.compiler.type.generic.relate.entity;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.common.Tense;
import org.harvey.compiler.common.collecction.BaseReference;
import org.harvey.compiler.common.tree.MultipleTree;
import org.harvey.compiler.type.generic.relate.ParameterizedRelationCache;
import org.harvey.compiler.type.generic.using.ParameterizedType;
import org.harvey.compiler.type.raw.RelationRawType;
import org.harvey.compiler.type.raw.RelationUsing;

/**
 * 把alias的形式保存下来, 一种静态的概念, 可能会随着alias的使用者不同而改变
 * <p>
 * 由于会用到最终origin的constructors, 而constructors的可能会用到本alias的泛型映射
 * 所以需要想办法, 把generic type map都完成映射
 * 1. 完成对GenericDefine的自我检查
 * 2. 进行GenericDefine到Map的映射
 * 3. 映射, 填充过程中, 可能会遇到
 * 4. 这个映射是不检查的, 全部加入..全部检查
 * - 不行
 * - alias 的generic define 失去意义
 * 3. 完成Parameterized到GenericDefine的检查, 这样才能完成映射
 * 4. 要完成Parameterized到GenericDefine的assign, 需要Parameterized到Parameterized的映射
 * 5. 那么就需要全部完成了...
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 16:13
 */
@Getter
@Deprecated
public class RelatedParameterizedAlias implements ParameterizedRelation {
    private final RelationRawType rawType;
    /**
     *
     */
    private final BaseReference definesStart;
    private final BaseReference definesEnd;
    private final RelatedParameterizedType origin;
    // 真的structure
    private final RelatedParameterizedStructure endOrigin;
    @Setter
    private Tense selfConsistentInspection;
    private RelatedParameterizedType aliasToEndOrigin;

    public RelatedParameterizedAlias(
            RelationRawType rawType,
            BaseReference definesStart,
            BaseReference definesEnd,
            RelatedParameterizedType origin,
            RelatedParameterizedStructure endOrigin) {
        this.rawType = rawType;
        this.definesStart = definesStart;
        this.definesEnd = definesEnd;
        this.origin = origin;
        this.endOrigin = endOrigin;
    }

    public RelatedParameterizedType aliasToEndOrigin(ParameterizedRelationCache parameterizedRelationCache) {
        if (aliasToEndOrigin != null) {
            return aliasToEndOrigin;
        }
        // 递归
        ParameterizedRelation parameterizedRelation = parameterizedRelationCache.get(origin.getRawType().getRawType());
        if (parameterizedRelation.isAlias()) {
            RelatedParameterizedAlias aliasOrigin = (RelatedParameterizedAlias) parameterizedRelation;
            ParameterizedType<RelationUsing> originToEnd = aliasOrigin.aliasToEndOrigin(parameterizedRelationCache)
                    .getValue()
                    .cloneThis(); // 使用 clone
            originToEnd.getTree().forEach((l, i) -> {
                MultipleTree<RelationUsing> node = l.get(i);
                RelationUsing value = node.getValue();
                ParameterizedType<RelationUsing> map = map(value, aliasOrigin);
                if (map != null) {
                    l.set(i, map.getTree());
                }
            });
            // originToEnd->end alias A<T,R> = End<T,T,R>;
            // 1. originToEnd clone
            // 2. originToEnd 结构变化
            // 3. originToEnd 置换
            //      用this.origin.child[i]->origin.generic[i]
        } else if (parameterizedRelation.isStructure()) {
            //
        }
        return aliasToEndOrigin;
    }

    /*
     * <pre>{@code
     * class MM<T> : AX<T, MM<T>, MMM<T>> { }
     *
     * class MMM<T> : AX<T, MM<T>, MMM<T>> { }
     *
     * class AX<T0, T1, T2> where T1 : AX<T0, MM<T0>, MMM<T0>>
     * where T2 : AX<T0, MM<T0>, MMM<T0>> {
     * // how?
     * }
     * }</pre>
     * <p>
     * 一般处理循环依赖, 就是用多级缓存
     * 这个怎么处理啊? 怎么处理多级缓存, 怎么分级啊, 分级之后怎么
     */

    /**
     * <p>
     * ?????
     * <p>
     * ?????
     * 先假设Structure作为入口(GenericDefine不能作为入口, 或者说暂且不讨论, 因为定义就是在Structure上的)
     * 先吧GenericDefine记录了, 没检查
     * 现在就需要把GenericDefine检查了,
     * GenericDefine发现涉及了类,
     * 1. 类是没检查过的Structure->先检查GenericDefine的RawType
     * 类是没检查过的Alias->先检查GenericDefine的界限的RawType
     * 类是没检查过的GenericDefine界限的RawType
     * 2. GenericParameters的RawType尝试注入, 能注入
     * T0能注入, 因为没有上下界
     * MM<T0>能注入T1, 因为 MM extends AX
     * MM<T0>能注入T2, 因为 MM extends AX
     * 3. 继承映射, MM 到 AX的继承路径是 MM < AX
     * 映射MM<T0>,到AX 就是 AX<T0,MM<T>,MMM<T>>
     * 此时检查AX的Parameterized list, 由于AX只到RawType阶段, 所以也只检查RawType
     * RawType姑且算通过了吧
     * AX<T,MM<T>,MMM<T>> 到 AX<T0,T1 extends AX<T0,MM<T0>,MMM<T0>>,T2 extends AX<T0,MM<T>,MMM<T>>>
     * T 确实是 T0 的子集
     * MM<T> 赋值给 MM<T> 确实行
     * MMM<T> 赋值给 MMM<T> 确实行
     * 检查GenericDefine, GenericDefine没有检查完毕
     * GenericDefine依赖于还没有检查的Structure, 但是正在进行时的Structure
     * 此时, 要先构建GenericDefine了, 没办法了
     * GenericDefine
     * 1. Generic Define 可以先检查 raw type的正确性
     * 2. 从任务栈中弹出, 上面的例子就是弹出
     * <p>
     * <p>
     * <p>
     * <p>
     * GenericDefine没有
     */
    private ParameterizedType<RelationUsing> map(RelationUsing v, RelatedParameterizedAlias aliasOrigin) {
        if (v.getRawType().isGenericDefine()) {
            RelatedGenericDefineReference reference = (RelatedGenericDefineReference) v.getRawType();
            int indexInPool = reference.getGenericDefineIndexBase() + reference.getGenericDefineIndexOffset();
            if (aliasOrigin.definesStart.getIndex() <= indexInPool &&
                indexInPool <= aliasOrigin.definesEnd.getIndex()) {
                // generic define on aliasOrigin
                // map to alias to generic
                int offset = indexInPool - aliasOrigin.definesStart.getIndex();
                // TODO 不对! 匹配就需要检查! 因为default!
                // class Structure[T1,T3,T4,T2,T5...]{}
                // alias B[T1,T2,T3,T4=DefaultType,T5...] = Structure[T1,T3,T4,T2,T5]
                // alias A[T,R] = B[T,R,T]
                // 使用:
                // Structure[Type1,Type1,DefaultType,Type2] = new A[Type1,Type2](); // 可行的!
                // ExampleObjectA[Structure[Type1,Type1,DefaultType,Type2]]
                // ExampleObjectA[A[Type1,Type2]]
                // 应该完全等价!
                // 1. 判断 A[Type1,Type2] 的自洽
                //      1. 判断A[Type1,Type2] 符合 A.GenericDefine
                //      2. 如果A的alias定义检查过, break, 否则进入1.3
                //      3. 判断A的origin, B[T,R,T], 也就是 B[T,R,T,DefaultType] 是否 符合 B.GenericDefine
                //           相当于返回1.1
                // 2. 这样懒加载不好, 定义A的alias的时候就应该完成A的自洽的检查
                // Tensor 三阶段, 如果在自洽检查中发现DOING, 就报错(循环定义), 如果发现TODO就ALIAS
                // Alias
                // 定义:
                // class Structure1[T1,T2 extends Structure[T1] = Structure[T1]]{} // 不行的, 不允许的
                //
                // 那我要怎么父类接收到一个类的泛型是子集的子类
                // Structure1[Type]
                // Structure1[Type,Structure[Type]]
                // .... 我请问了, 有什么情况需要用到在父类用泛型吗?
                // ? c sharp 什么都可以加?
                return this.origin.getValue().getChild(offset);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    @Override
    public boolean isAlias() {
        return true;
    }
}
