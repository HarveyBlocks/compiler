package org.harvey.compiler.type;

import org.harvey.compiler.io.stage.CompileStage;

/**
 * 1. alias rawType 映射
 * 2. normal Type extends 和 interfaces
 * 3. generic define 和 parameterized type 同时构建关系,
 *      TODO 构建后, 检查后, 需要保存构建的ParameterizedType吗?
 *      我认为是不需要的, generic define是需要保存的
 * 4. ParameterizedType对自生的检查, GenericMessageList是否符合定义的规范
 * 5. ParameterizedType在赋值上的检查(耦合而不可分割!欸)
 *      alias generic type 检查
 *      normal type generic 检查
 *      extends 和 interface的generic 检查
 * 5. alias 完成映射
 *       已经检查过了, alias map就简单了
 *       只需要在alias的定义处map, 其他地方再说
 *       因为考虑到在反射的时候, 可能会需要直接类型(在源码上写的是alias), 反射也能够依据alias去获取
 * 6. 对字段的类型, 函数返回值类型, 函数参数类型进行解析
 * 7. 对函数的重载进行检查
 * 8. 表达式和代码块进行解析
 * 9. finished
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-16 19:09
 */
public enum RelationshipBuildStage {
    /**
     * 由于需要直到alias 的映射对象是interface还是structure, 是sealed 还是 非sealed,所以先alias进行map. 所有alias都要先
     */
    ALIAS_RAW_TYPE_MAP,
    /**
     * 搞清楚RawType之间的关系
     * <p>
     * 不允许循环依赖, 确认没有循环依赖
     * 建立关系
     */
    STRUCTURE_RAW_TYPE_RELATIONSHIP,
    VISIBLE_CHECK,
    /**
     * alias的关系看作继承
     * GenericDefine不允许循环依赖(也不允许前向依赖), 确认没有循环依赖
     * ParametrizedType允许不涉及定义, 没问题
     * GenericType涉及到的ParameterizedType解析之后,
     * ParameterizedType涉及到GenericType之后, 把GenericType看作一个普通类型
     * Generic没有下限则认为是所有类的子类, Generic如果没有上限则认为是所有类的父类
     * 检查关系
     * 检查完毕后, 结构和内容都不用变
     */
    GENERIC_DEFINE_AND_UPPER_PARAMETERIZED_TYPE_CHECK,
    /**
     * 完成对Alias的编译文件级别的映射
     * class M[T1]{
     * alias X[TT1,TT2,TT3] = Outer[T1,TT2,M[TT3],Y[TT2],TT1]
     * }
     * alias Outer[T1,T2,T3,T4,T5] = OtherFile[T5,X[T4],T3,T2,T1]
     * <p>
     * 1. X 涉及Outer,
     * 2. Outer也是alias, Outer涉及OtherFile,OtherFile是structure, 此时只需要检查Outer能成功匹配(第二阶段完成),所以Outer不动
     * 3. X 仅仅需要完成一个跨越Outer的映射
     * 4. 遍历(遍历第一层)Outer(Using)的参数列表, 做一个映射:
     * 含义                  映射内容           含义
     * Outer.[T1]      ->    T1             M.[T1]
     * Outer.[T2]      ->    TT2            X.[TT2]
     * Outer.[T3]      ->    M[TT3]         M[X.[TT3]]
     * Outer.[T4]      ->    Y[TT2]         Y[X.[TT2]]
     * Outer.[T5]      ->    TT1            X.[TT1]
     * 5. 遍历(遍历序列化后的整个参数列表)OtherFile的List, 发现有Reference指向的是Alias的Generic, 则全部替换成映射表中的
     * 6. 映射较为简单
     * <p>
     * <p>
     * <p>
     * 对于序列化来说, Alias结构不变, 内容变化
     */
    ALIAS_MAP,
    /**
     * 如果读出Type是文件是高于Statement的, 就可以使用FinishStage
     */
    FINISHED;

    public static RelationshipBuildStage get(CompileStage stage) {
        if (stage == CompileStage.STATEMENT) {
            return STRUCTURE_RAW_TYPE_RELATIONSHIP;
        } else if (stage == CompileStage.COMPILED || stage == CompileStage.LINKING) {
            return FINISHED;
        } else {
            return null;
        }
    }


}
