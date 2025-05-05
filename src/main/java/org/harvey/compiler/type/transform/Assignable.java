package org.harvey.compiler.type.transform;

import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedLocalParameterizedType;
import org.harvey.compiler.type.generic.relate.entity.RelatedParameterizedType;
import org.harvey.compiler.type.raw.RelationUsing;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-26 20:30
 */
public interface Assignable {


    void assign(RelatedGenericDefine from);

    // 使用
    void assign(RelatedParameterizedType from);

    /**
     * 特别的, 不考虑{@link RelatedParameterizedType}
     */
    void assign(RelationUsing from);

    void assign(RelatedLocalParameterizedType from);

    void selfConsistent();
    /*
     * 对于一个parameterized type怎么赋值啊
     *
     * A<A,B,C> -> B <C,D,A>
     *
     * A 是 B 之间, 每一个都能合适匹配
     * Parameterized Type 需要递归地检查啊
     * RawType/GenericDefine <- ParameterizedType
     * ParameterizedType <- RawType/GenericDefine
     * ParameterizedType <- ParameterizedType
     * RawType要获取自己的父类的RelatedParameterizedType, 要怎么获取啊?
     * 获取了之后, 注入GenericDefine, 要怎么替换啊? Map, 暂且算解决
     *
     * RelationRawType都转化, 把参数都换成嵌套的吗? 这合适吗?
     *
     * 换了之后呢? 依据parameterizedType, 把参数都替换了, 然后, 替换的时候检查替换得是否合理
     *
     * 1. 构建GenericDefine定义处的关系, 用GenericDefine来暂替里面的GenericDefine内容, 并构建映射表, 方便泛型参数进入时使用
     * 2. 构建的时候, 需要检查GenericDefine替换进入时是否合法,
     *      - TODO 怎么检查是否合法
     *      - ParameterizedType 到 GenericDefine的检查
     *      - 需要注意的时 ParameterizedType 自身内部也有可能带有参数
     * 3. 构建Parameterized的时候, 需要参数填入. 参数填入的时候, 检查是否符合GenericDefine, 然后再填入
     *      - ParameterizedType 到 GenericDefine的检查
     * 4. ParameterizedType 到 GenericDefine的检查
     *      - 要检查范围Supper和Lower之间, 又转到ParameterizedType和ParameterizedType之间的转换
     * 5. ParameterizedType和ParameterizedType之间的转换
     *      1. 由于ParameterizedType存在 default 和 multiple的语法糖, 所以 先把default补全
     *      2. 如果from的Raw和to的Raw一致, 严格匹配, multiply的也一个不能少
     *      3. 如果from的Raw是to的Raw的子类, from带着泛型向上转, 一直转, 直到转到to的同级type为止
     *      4. 执行2
     * 6. child的ParameterizedType向parent的转换
     *      - 要怎么获取到一条通向正确的parent的路呢? 广度优先搜索, 搜索出一条路径来(是最短路径)
     *      - 考虑到菱形继承
     *          A
     *         / \
     *        B   C
     *         \ /
     *          D
     *          D Generic的范围一定<=C.Generic, 且 <= B.Generic, 所以向上转换不需要检查是否能转换
     *          只要能转换成D, 就一定能转换成A
     * 7. 考虑有哪些东西需要存储, 有哪些东西不用存储
     * 8. contractor怎么办? 怎么读入?
     * 9. contractor可以有泛型, 但一定要能通过泛型参数推断出来, 否则就GG
     * 10. 要注意到ParameterizedType中可能有Keyword, GenericDefine, Structure 和 Alias
     *      GenericDefine中会有GenericDefine, Structure 和 Alias
     *      GenericDefine在ParameterizedType不能有Children, A[T,V extends T[Element]] 就不行
     * 11. 序列化阶段就要考虑一下Serializer了他妈的
     *
     **/
}
