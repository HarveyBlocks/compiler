package org.harvey.compiler.type.transform;

import java.util.List;

/**
 * TODO
 * 三者关系:
 * - using
 * - define
 *      - standard 用于检查函数签名重名, 退化到using和define的匹配的关系
 *      - implement 用于检查函数签名重名, 退化到using和define的匹配的关系
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-28 16:44
 */
public class CallableSignatureMatcherImpl implements CallableSignatureMatcher {
    private final AssignableFactory assignableFactory;

    public CallableSignatureMatcherImpl(AssignableFactory assignableFactory) {
        this.assignableFactory = assignableFactory;
    }

    @Override
    public boolean shouldDeduplication(CallableSignature one, CallableSignature another) {
        // 如果 A-B != 空集 && B-A != 空集 就算 函数签名不同, 两个函数可以同时存在
        // 否则, 如果 A==B, 肯定 NG
        // 如果 A 是 B 的 真子集, A 完全不会被调用到, B 有机会调用
        // 如果 B 是 A 的 真子集, B 完全不会被调用到, A 有机会调用
        // 所以算冲突
        // 以一种方式去计算差集
        // 在调用时还需要再次检查, 如果发现重复的, 就在使用处报错
        //
        // A-B != 空集, 等价于 A 不是 B 的 子集
        // ! (A 是 B 的 子集)
        // 也就是说 !(A 是 B 的子集 || B 是 A 的子集) 时
        // if A 是 B 的子集 -> 签名重名
        // elif B 是 A 的子集 -> 签名重名
        // else -> 签名没有重名
        //
        // 也就是说, 转换成了判断A是B的子集
        //
        // 如何判断两个函数签名, 一个是另一个的子集呢?
        // strict参数数量不同是有差集的
        // 只比较type, 不考虑所谓default和multiply吗?
        // 1. 比较一个 单个参数类型 之间的子集关系
        //      TYPE_A 是 TYPE_B 的子集 -> 所有的 TYPE_A 都是 TYPE_B ->
        //      TYPE_B is to and TYPE_A is from and assignable
        // 2. 比较参数列表之间的子集关系
        //      说一个参数列表是另一个参数列表的子集->
        //      PARAM_LIST_A 填充了 Default 之后(也就是说, Default在编译阶段并不会缺省), 赋值到 PARAM_LIST_B, 能成功
        //      A: (int, int=default,int=default)
        //      B: (int, int...)
        //      我们说, A 是 B 的子集, 如果这里不检查出来, 外界调用的是否总是要报错的, A 永远没有机会被调用
        //      不存在一种情况, A 能匹配, 而 B 不能匹配的
        //
        //      反过来看 B 是不是 A 的子集
        //      不是, B 有情况不能赋值到A上
        //      另一种情况:
        //      A: (int, int,bool=default,int=default)
        //      B: (int, int,int)
        //      显然 A 不是 B 的子集
        //      B 是 A 的子集吗? 答案是, 是的, B 是 A 的子集
        //      从中可以看出, 在匹配时, 要检查 A 是不是和 B 同名
        //          检查 A 是否是 B 的子集时
        //          A 需要 将 default 忽略, 赋值到 B 的签名上去, 但是, 做这个赋值的时候, B 不能忽略了default
        //          对于B来说, 匹配 A 时, 不知道 A 来匹配的目的是检查重名还是参数使用, B 不知道
        //
        // 3. 比较泛型参数列表的子集关系
        //      同理, 一个看作from, 一个看作to, 看能不能从 from 赋值到 to, 能就说明重复
        // 4. 只要满足
        //      参数列表不是子集关系
        //      或
        //      参数列表不是子集关系,
        //      即可表示函数签名不同, 函数即可同时存在
        //
        // 恰好, match方法的需求刚好是只检查子集, 所以本方法刚好使用这个方法

        return false;
    }



    @Override
    public int match(CallableSignature standard, CallableSignature implement) {
        // 对于这个方法
        // 任务是查看, 依据 standard 到 implement 构成一个映射之后
        // 每次调用向 standard 填入任意参数, implement 总能够接收
        // 凡是能填入 standard 的, 一定能填入 implement
        // 也就是说, standard 是 implement 的子集
        // 下面考虑检查 standard 是 implement 的子集时, 对standard 的 default和 multiply的处理
        // 对于standard的default, 有default的不被缺省(见上)
        // 对于standard的multiply
        //   - 如果 implement不是multiply
        //          前面正常匹配, 后面不能加多的东西
        //      - 如果implement是multiply的
        //          - 如果standard是multiply,且multiply的元素的类型和implement的元素类型是assignable->能匹配
        //                  standard: (bool,bool,int...)
        //                  implement: (bool,bool,int...) 允许
        //          - 如果standard不是multiply, 而是Array,且数组元素的类型和implement是assignable->能匹配
        //          - 如果standard是multiply,且multiply的元素的类型和implement的元素类型是assignable->能匹配
        //                  standard: (bool,bool,int[])
        //                  implement: (bool,bool,int...) 允许
        //          - 如果standard从后面几个开始....
        //                  假设implement: (bool,bool,int...)
        //                  下面的standard是允许的:
        //                  (bool,bool, int)
        //                  (bool,bool, int,int)
        //                  (bool,bool, int,int...)
        //                  (bool,bool, int,int...)
        //                  (bool,bool, int,int,int...)
        //              但是:
        //                  (bool,bool, int,int[]) 是不允许的, 因为array是不可变长的
        //        所以, 如果implement是 `Type...`
        //              对应的standard的位置是
        //                  1. Type... 过
        //                  2. Type[], 且后面没有了 过
        //                  4. Type, 后面连续零个或多个Type, 最后是零个或一个Type... 过
        //                  5. 否则, 不过
        //        综上, 只有符合[(Type...)|(Type[])|((Type)+(Type...)?)]->过,否则不过

        // 5. 有些时候, 泛型能够依据Param推测出来, 考虑这一点对函数签名匹配的影响
        //       思考, 是否存在一种情况, implement是standard的子集
        //       但是在考虑了Param推测机制之后, 能在implement上匹配, 但不能在standard上匹配
        //      也就是说, 在考虑了param推测机制之后, default直接放在上面是否不再适用
        //       <T1,T2,T3,T5=default,T6...>(T2 t)
        //       <T1,T2,T3=default,T5=default,T6...>(T2 t)
        //      不会影响, 我们总是考虑, 适用者能给出对于任意一个都正确的GenericList
        //      所以, 对于那些不能确定的, 我们总是假设能被以某种方式正确给出
        //          假设不存在这种正确方式, 也就是说standard其实是implements的子集, 但是误以为不是子集了, 所以在定义层面能通过
        //          这样对用户是不友好的, 对编译器是没有坏处的
        //      standard: <T1,T2,T3=default,T5,T6=default>
        //      using:<T1,T2,T3,T5> 可以匹配standard
        //      找一种implement, 不会匹配standard, 也能匹配using, 且不是standard
        //      可以的, 可以param占位, 可以default占位, 可以的
        //      下面是方法论:
        //      1. 先从param获取哪些generic define可以从param推测出来
        //      2. 那些可以
        //


        // 下面不是函数签名重名的内容, 是函数的使用using时的注意
        // 有些时候, 泛型能够依据Param推测出来, 下面是考虑了Param推测的Generic On Callable
        //      1. 对于泛型一定要能从Param推测的情况(例如定义在构造器上的泛型)
        //          这种函数不能在调用的时候决定泛型的类型
        //      2. 对于泛型可以存在从Param上无法推测的情况
        //          这种函数在调用的时候, 泛型定义上的类型, 优先于从函数的推测, 也就是说, 不对泛型从param推测
        //          泛型列表如果不写, 从param推测
        //          泛型列表如果写了一点, 从前往后写, 先从param推测,
        //          param给出了依据的generic, 如果有default, 调用时可不用写
        //          param给出了依据的generic, 如果在multiply, 调用时可不用写
        //          param给出了依据的generic, 如果在strict, 且在尾巴上, 可忽略, 也就是说, 这些有依据的generic看作有default
        //          param给出了依据的generic, 如果在strict, 且不在尾巴上, 即使可以推测, 不可忽略, 调用时要写
        //          在generic list上决定的type, 优先级比从pram推测的要高, 用generic list的, 来检查param是否正确
        //      流程:
        //          if standard.point 在 strict 上:
        //              if '开启从param推理'==false && using.point 能匹配 standard.point :
        //                  using.point++; // 用于校验param的正确性
        //                  using 放在 结果位;
        //                  continue;
        //              elif '开启从param推理'==false && using.point 不能匹配 standard.point :
        //                  if 能从param推测 standard.point :
        //                      '开启从param推理' = true;
        //                      采用pram的generic, 用pram的位置占位
        //                  else:
        //                      throw 需要补充完整generic list 因为 strict 不能从param推测
        //              elif using.point 能匹配 standard.point :
        //                  using.point++; // 用于校验param的正确性
        //                  continue;
        //              else:
        //                  if 能从param推测 standard.point :
        //                      采用pram的generic, 用pram的位置占位
        //                  else:
        //                      throw 需要补充完整generic list 因为 strict 不能从param推测
        //          elif standard.point 在default上:
        //              if using.point 能匹配 standard.point:
        //                  using.point++; // 用于校验param的正确性
        //                  using 放在 结果位;
        //              elif 能从param推测 standard.point:
        //                  采用pram的generic, 用pram的位置占位
        //              else:
        //                  采用default上的值
        //                  continue;
        //          elif standard.point 在multiply上:
        //              遍历剩下的using.point:
        //                  如果不剩,不能从param推导->过
        //                  如果还有->using.point++; // 用于校验param的正确性
        //                  如果不剩, 能从param推导->
        //                      采用pram的generic, 用pram的位置占位
        //
        // 注意: generic被标注了multiply的generic type, 不能单独作为type 出现, 只能作为generic list 的 argument出现
        //

        return 0;
    }
}
